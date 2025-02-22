package org.obc.core.capsule;

import com.google.protobuf.ByteString;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obc.common.obcApplicationContext;
import org.obc.common.utils.ByteArray;
import org.obc.common.utils.FileUtil;
import org.obc.core.ChainBaseManager;
import org.obc.core.Constant;
import org.obc.core.Wallet;
import org.obc.core.capsule.ExchangeCapsule;
import org.obc.core.config.DefaultConfig;
import org.obc.core.config.args.Args;
import org.obc.core.db.Manager;
import org.obc.core.db.StorageMarket;
import org.obc.core.exception.ItemNotFoundException;

@Slf4j
public class ExchangeCapsuleTest {

  private static final String dbPath = "output_exchange_capsule_test_test";
  private static final String OWNER_ADDRESS;
  private static final String OWNER_ADDRESS_INVALID = "aaaa";
  private static final String OWNER_ACCOUNT_INVALID;
  private static final long initBalance = 10_000_000_000_000_000L;
  private static Manager dbManager;
  private static ChainBaseManager chainBaseManager;
  private static StorageMarket storageMarket;
  private static obcApplicationContext context;

  static {
    Args.setParam(new String[]{"--output-directory", dbPath}, Constant.TEST_CONF);
    context = new obcApplicationContext(DefaultConfig.class);
    OWNER_ADDRESS = Wallet.getAddressPreFixString() + "548794500882809695a8a687866e76d4271a1abc";
    OWNER_ACCOUNT_INVALID =
        Wallet.getAddressPreFixString() + "548794500882809695a8a687866e76d4271a3456";
  }

  /**
   * Init data.
   */
  @BeforeClass
  public static void init() {
    dbManager = context.getBean(Manager.class);
    chainBaseManager = context.getBean(ChainBaseManager.class);
    storageMarket = new StorageMarket(chainBaseManager.getAccountStore(),
        chainBaseManager.getDynamicPropertiesStore());
  }

  /**
   * Release resources.
   */
  @AfterClass
  public static void destroy() {
    Args.clearParam();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }

  /**
   * create temp Capsule test need.
   */
  @Before
  public void createExchangeCapsule() {
    chainBaseManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(0);

    long now = chainBaseManager.getHeadBlockTimeStamp();
    ExchangeCapsule exchangeCapsulee =
        new ExchangeCapsule(
            ByteString.copyFromUtf8("owner"),
            1,
            now,
            "abc".getBytes(),
            "def".getBytes());

    chainBaseManager.getExchangeStore().put(exchangeCapsulee.createDbKey(), exchangeCapsulee);

  }

  @Test
  public void testExchange() {
    long sellBalance = 100000000L;
    long buyBalance = 100000000L;

    byte[] key = ByteArray.fromLong(1);

    ExchangeCapsule exchangeCapsule;
    try {
      exchangeCapsule = chainBaseManager.getExchangeStore().get(key);
      exchangeCapsule.setBalance(sellBalance, buyBalance);

      long sellQuant = 1_000_000L;
      byte[] sellID = "abc".getBytes();

      long result = exchangeCapsule.transaction(sellID, sellQuant);
      Assert.assertEquals(990_099L, result);
      sellBalance += sellQuant;
      Assert.assertEquals(sellBalance, exchangeCapsule.getFirstTokenBalance());
      buyBalance -= result;
      Assert.assertEquals(buyBalance, exchangeCapsule.getSecondTokenBalance());

      sellQuant = 9_000_000L;
      long result2 = exchangeCapsule.transaction(sellID, sellQuant);
      Assert.assertEquals(9090909L, result + result2);
      sellBalance += sellQuant;
      Assert.assertEquals(sellBalance, exchangeCapsule.getFirstTokenBalance());
      buyBalance -= result2;
      Assert.assertEquals(buyBalance, exchangeCapsule.getSecondTokenBalance());

    } catch (ItemNotFoundException e) {
      Assert.fail();
    }

  }


}
