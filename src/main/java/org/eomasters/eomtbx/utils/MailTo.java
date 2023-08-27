package org.eomasters.eomtbx.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Builder for mailto URIs.
 */
public class MailTo {

  private final String[] recipients;
  private String[] carbonCopies;
  private String subject;
  private String body;

  public MailTo(String mainAddress, String... addresses) {
    ArrayList<String> addressList = new ArrayList<>();
    addressList.add(mainAddress);
    Collections.addAll(addressList, addresses);
    this.recipients = addressList.toArray(String[]::new);
  }

  public MailTo cc(String... adresses) {
    this.carbonCopies = adresses;
    return this;
  }

  public MailTo subject(String subject) {
    this.subject = subject.replace(" ", "%20");
    return this;
  }

  public MailTo body(String body) {
    if(body.length() > 2000) {
      throw new IllegalArgumentException("Body must be less than 2000 characters");
    }
    this.body = body.replace(" ", "%20");
    return this;
  }

  public URI toURI() {
    StringBuilder sb = new StringBuilder("mailto:?to=");
    sb.append(String.join(",", recipients));
    if (carbonCopies != null) {
      sb.append("&cc=");
      sb.append(String.join(",", carbonCopies));
    }
    if (subject != null) {
      sb.append("&subject=");
      sb.append(subject);
    }
    if (body != null) {
      sb.append("&body=");
      sb.append(body);
    }
    return URI.create(sb.toString());
  }
}
