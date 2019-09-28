package org.forgerock.ampwdgen;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

// Small service to encrypt AM passwords
// Test with:
// curl -X POST -d "key=foo" -d "password=bar"  -d "hash=true" localhost:8888
// Where key is the AM instance key used for encryption
// password is the clear text password you want to encrypt
// hash=true (optional - you can omit) controls whether the password should be hashed before being encrypted
// Because this may be deployed as a cloud function, DO NOT log or print the POST data request
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    final String portString = System.getenv("PORT");
    final int port = portString == null ? 8888 : Integer.parseInt(portString);

    vertx.createHttpServer().requestHandler(req -> {
      System.out.println("request " + req.headers());

      req.setExpectMultipart(true);

      req.bodyHandler( b -> {
        var key = req.getFormAttribute("key");
        var pw = req.getFormAttribute("password");

        if( key == null || pw == null ) {
          req.response().
            setStatusCode(400)
            .setStatusMessage("Missing the key= and/or password= in the POST request")
            .end();
            return;
        }
        // hash flag controls if we should hash the password before encrypting
        var h = req.getFormAttribute("hash");
        if( h != null && h.equals("true"))  {
          pw =  AmCrypto.hash(pw);
        }
        req.response()
          .putHeader("content-type", "text/plain")
          .end(AmCrypto.encrypt(key,pw));
      });

    }).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port " + port);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
