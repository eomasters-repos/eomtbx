/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
 * ======================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.Collections;
import org.eomasters.eomtbx.utils.MailTo.MailToException;
import org.junit.jupiter.api.Test;

class MailToTest {

  @Test
  void toCreation() throws MailToException {
    URI uri1 = new MailTo("test@email.com").toUri();
    assertEquals("mailto:?to=test@email.com", uri1.toString());

    URI uri2 = new MailTo("test@email.com", "another@yahoo.com").toUri();
    assertEquals("mailto:?to=test@email.com,another@yahoo.com", uri2.toString());
  }

  @Test
  void testInvalidMails() {
    assertThrows(MailToException.class, () -> new  MailTo("illegal@@email.com"));
    assertThrows(MailToException.class, () -> new  MailTo("test@email.com", "illegal@@email.com"));
  }

  @Test
  void testCc() throws MailToException {
    URI uri1 = new MailTo("tEst@mail.org").cc("copyTo@other.one").toUri();
    assertEquals("mailto:?to=tEst@mail.org&cc=copyTo@other.one", uri1.toString());
  }

  @Test
  void testCcInvalidMails() throws MailToException {
    MailTo mailTo = new MailTo("valid@email.com");
    assertThrows(MailToException.class, () -> mailTo.cc("illegal@@email.com"));
    assertThrows(MailToException.class, () -> mailTo.cc("another@mail.org", "illegal@@email.com"));
  }

  @Test
  void testSubject() throws MailToException {
    URI uri1 = new MailTo("test@mail.org").subject("Test subject").toUri();
    assertEquals("mailto:?to=test@mail.org&subject=Test%20subject", uri1.toString());
  }

  @Test
  void testSubjectToLong() throws MailToException {
    MailTo mailTo = new MailTo("test@mail.org");
    assertThrows(MailToException.class, () -> mailTo.subject(genLongText(MailTo.MAX_SUBJECT_LENGTH + 1)));
  }

  @Test
  void testBody() throws MailToException {
    URI uri1 = new MailTo("test@mail.org").body("Test body").toUri();
    assertEquals("mailto:?to=test@mail.org&body=Test%20body", uri1.toString());
  }

  @Test
  void testBodyToLong() throws MailToException {
    MailTo mailTo = new MailTo("test@mail.org");
    assertThrows(MailToException.class, () -> mailTo.body(genLongText(MailTo.MAX_BODY_LENGTH + 1)));
  }

  @Test
  void testAll() throws MailToException {
    URI uri1 = new MailTo("test@mail.org", "one@mail.de", "two@mail.com")
        .cc("copy@mail.org").body("Test body").subject("Test subject").toUri();
    assertEquals("mailto:?to=test@mail.org,one@mail.de,two@mail.com&cc=copy@mail.org&subject=Test%20subject&body=Test%20body",
        uri1.toString());
  }

  @Test
  void testAllToLong() throws MailToException {
    MailTo mailTo = new MailTo("test@mail.org", "onelongAddress@mail.de", "two@mailprovider.com")
        .cc("copy@mail.org", "abcdefg@yourmailhost.org")
        .subject(genLongText(MailTo.MAX_SUBJECT_LENGTH)).body(genLongText(MailTo.MAX_BODY_LENGTH));
    assertThrows(MailToException.class, mailTo::toUri);
  }

  private static String genLongText(int i) {
    return String.join("", Collections.nCopies(i, "a"));
  }

}
