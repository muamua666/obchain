package org.obc.core.services.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.obc.core.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.obc.api.GrpcAPI.BytesMessage;
import org.obc.api.GrpcAPI.NfParameters;


@Component
@Slf4j(topic = "API")
public class CreateShieldNullifierServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      PostParams params = PostParams.getPostParams(request);
      NfParameters.Builder build = NfParameters.newBuilder();
      JsonFormat.merge(params.getParams(), build);
      BytesMessage result = wallet.createShieldNullifier(build.build());
      response.getWriter().println(JsonFormat.printToString(result, params.isVisible()));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
