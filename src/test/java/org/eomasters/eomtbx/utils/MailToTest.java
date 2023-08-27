package org.eomasters.eomtbx.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.Test;

class MailToTest {

  @Test
  void toCreation() {
    URI uri1 = new MailTo("test@email.com").toURI();
    assertEquals("mailto:?to=test@email.com", uri1.toString());

    URI uri2 = new MailTo("test@email.com", "another@yahoo.com").toURI();
    assertEquals("mailto:?to=test@email.com,another@yahoo.com", uri2.toString());
  }

  @Test
  void testCC() {
    URI uri1 = new MailTo("test@mail.org").cc("copyTo@other.one").toURI();
    assertEquals("mailto:?to=test@mail.org&cc=copyTo@other.one", uri1.toString());
  }

  @Test
  void testSubject() {
    URI uri1 = new MailTo("test@mail.org").subject("Test subject").toURI();
    assertEquals("mailto:?to=test@mail.org&subject=Test%20subject", uri1.toString());
  }

  @Test
  void testBody() {
    URI uri1 = new MailTo("test@mail.org").body("Test body").toURI();
    assertEquals("mailto:?to=test@mail.org&body=Test%20body", uri1.toString());
  }

  @Test
  void testAll() {
    URI uri1 = new MailTo("test@mail.org", "one@mail.de", "two@mail.com")
        .cc("copy@mail.org").body("Test body").subject("Test subject").toURI();
    assertEquals("mailto:?to=test@mail.org,one@mail.de,two@mail.com&cc=copy@mail.org&subject=Test%20subject&body=Test%20body",
        uri1.toString());
  }
}