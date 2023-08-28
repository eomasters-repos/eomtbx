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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Builder for mailto URIs.
 */
public class MailTo {

  public static final int MAX_SUBJECT_LENGTH = 150;
  public static final int MAX_BODY_LENGTH = 1800;
  public static final int MAX_MAILTO_LENGTH = 2040;
  private static final String MAIL_OWASP_REGEX = "^[a-zA-Z0-9_+&*-]+"
      + "(?:\\.[a-zA-Z0-9_+&*-]+)*@"
      + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

  private final String[] recipients;
  private String[] carbonCopies;
  private String subject;
  private String body;

  /**
   * Creates a new MailTo with the given main address and optional additional addresses. The provided addresses are
   * validated. If an address is invalid, a MailToException is thrown.
   *
   * @param mainAddress the main address
   * @param addresses   additional addresses
   * @throws MailToException if an address is invalid
   */
  public MailTo(String mainAddress, String... addresses) throws MailToException {
    recipients = validateMailAddresses(mainAddress, addresses);
  }

  /**
   * Adds carbon copies to the mailto URI. The provided addresses are validated. If an address is invalid, a
   * MailToException is thrown.
   *
   * @param firstAddress the first address
   * @param addresses    additional addresses
   * @return this instance
   * @throws MailToException if an address is invalid
   */
  public MailTo cc(String firstAddress, String... addresses) throws MailToException {
    this.carbonCopies = validateMailAddresses(firstAddress, addresses);
    return this;
  }

  /**
   * Sets the subject of the mailto URI. The subject is encoded.
   * <p>The length of the subject must be less than {@value MAX_SUBJECT_LENGTH} characters.</p>
   *
   * @param subject the subject text
   * @return this instance
   * @throws MailToException if the subject is too long
   */
  public MailTo subject(String subject) throws MailToException {
    if (subject.length() > MAX_SUBJECT_LENGTH) {
      throw new MailToException("Subject must be less than " + MAX_SUBJECT_LENGTH + " characters");
    }
    this.subject = encodeText(subject);
    return this;
  }

  /**
   * Sets the body of the mailto URI. The body is encoded.
   * <p>The length of the body must be less than {@value MAX_BODY_LENGTH} characters.</p>
   *
   * @param body the body text
   * @return this instance
   * @throws MailToException if the body is too long
   */
  public MailTo body(String body) throws MailToException {
    if (body.length() > MAX_BODY_LENGTH) {
      throw new MailToException("Body must be less than " + MAX_BODY_LENGTH + " characters");
    }
    this.body = encodeText(body);
    return this;
  }

  /**
   * Creates the mailto URI.
   * <p>The length of the URI must be less than {@value MAX_MAILTO_LENGTH} characters.</p>
   *
   * @return the URI for the mailto
   * @throws MailToException if the URI could not be created
   */
  public URI toUri() throws MailToException {
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
    String mailToString = sb.toString();
    if (mailToString.length() > MAX_MAILTO_LENGTH) {
      throw new MailToException("Body must be less than 1800 characters");
    }
    return URI.create(mailToString);
  }

  private String encodeText(String body) {
    String encoded = URLEncoder.encode(body, StandardCharsets.UTF_8);
    return encoded.replace("+", "%20");
  }

  private String[] validateMailAddresses(String mainAddress, String[] addresses) throws MailToException {
    ArrayList<String> addressList = new ArrayList<>();
    addressList.add(mainAddress);
    Collections.addAll(addressList, addresses);
    Optional<String> optional = addressList.stream().filter(s -> !isValidEmailAddress(s)).findFirst();
    if (optional.isPresent()) {
      throw new MailToException("Invalid email address: '" + optional.get() + "'");
    }
    return addressList.toArray(String[]::new);
  }

  /**
   * Checks whether the given address is a valid email address.
   *
   * @param address the address to check
   * @return true if the address is valid
   */
  boolean isValidEmailAddress(String address) {
    return Pattern.matches(MAIL_OWASP_REGEX, address);
  }

  /**
   * Exception thrown if the mailto URI could not be created.
   */
  public static class MailToException extends Exception {

    /**
     * Creates a new MailToException with the given message.
     *
     * @param message the message
     */
    public MailToException(String message) {
      super(message);
    }

  }
}
