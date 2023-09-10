/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.vbkmrks;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import org.eomasters.eomtbx.EomToolbox;
import org.esa.snap.core.datamodel.GeoPos;

/**
 * The class which stores the view bookmarks.
 */
public class ViewBookmarks implements Cloneable {

  /**
   * The maximum number of bookmarks.
   */
  public  static final int MAX_BOOKMARKS = 7;
  private static final String VIEW_BOOKMARKS_ID = "viewBookmarks";
  private final Preferences vbPreferences;
  ArrayList<Bookmark> bookmarks = new ArrayList<>();

  /**
   * Creates a new ViewBookmarks instance. Only used by the singleton and testing.
   */
  ViewBookmarks(Preferences vbPreferences) {
    this.vbPreferences = vbPreferences;
  }

  /**
   * Returns the ViewBookmarks instance.
   *
   * @return the ViewBookmarks instance
   */
  public static ViewBookmarks getInstance() {
    return ViewBookmarks.InstanceHolder.INSTANCE;
  }

  /**
   * Returns the bookmarks.
   *
   * @return the bookmarks
   */
  public ArrayList<Bookmark> getBookmarks() {
    return bookmarks;
  }

  /**
   * Returns the preferences.
   *
   * @return the preferences
   */
  public Preferences getPreferences() {
    return vbPreferences;
  }

  @Override
  public ViewBookmarks clone() {
    ViewBookmarks clone;
    try {
      clone = (ViewBookmarks) super.clone();
    } catch (CloneNotSupportedException e) {
      clone = new ViewBookmarks(vbPreferences);
    }

    clone.bookmarks = new ArrayList<>();
    for (Bookmark bookmark : bookmarks) {
      clone.bookmarks.add(bookmark.clone());
    }
    return clone;
  }

  /**
   * Adds a bookmark.
   *
   * @param bookmark the bookmark to add
   * @throws Exception if the bookmark already exists or the maximum number of bookmarks is reached
   */
  public void add(Bookmark bookmark) throws Exception {
    if (bookmarks.contains(bookmark)) {
      throw new Exception("Bookmark already exists");
    } else if (bookmarks.size() >= MAX_BOOKMARKS) {
      throw new Exception("Maximum number of bookmarks reached");
    }
    bookmarks.add(bookmark);
  }

  private static class InstanceHolder {

    private static final ViewBookmarks INSTANCE = new ViewBookmarks(
        EomToolbox.getPreferences().node(ViewBookmarks.VIEW_BOOKMARKS_ID));
  }


  /**
   * The bookmark holding the information about a view to recreate it.
   */
  public static class Bookmark implements Cloneable {

    private String name;
    private double rotation;
    private BookmarkRoi bookmarkRoi;
    private Rectangle2D viewBoundary;


    /**
     * Creates a new Bookmark.
     *
     * @param name        the name of the bookmark
     * @param bookmarkRoi the ROI of the bookmark
     * @param rotation    the rotation of the bookmark
     */
    public Bookmark(String name, BookmarkRoi bookmarkRoi, double rotation) {
      this.name = name;
      this.bookmarkRoi = bookmarkRoi;
      this.rotation = rotation;
    }

    /**
     * Returns the name of the bookmark.
     *
     * @return the name of the bookmark
     */
    public String getName() {
      return name;
    }

    /**
     * Returns the rotation of the bookmark.
     *
     * @return the rotation of the bookmark
     */
    public double getRotation() {
      return rotation;
    }


    /**
     * Returns the ROI of the bookmark.
     *
     * @return the ROI of the bookmark
     */
    public BookmarkRoi getRoi() {
      return bookmarkRoi;
    }


    @Override
    public Bookmark clone() {
      try {
        Bookmark clone = (Bookmark) super.clone();
        clone.name = name;
        clone.rotation = rotation;
        clone.bookmarkRoi = bookmarkRoi.clone();
        return clone;
      } catch (CloneNotSupportedException e) {
        return new Bookmark(name, bookmarkRoi.clone(), rotation);
      }
    }
  }

  /**
   * The ROI of the bookmark.
   */
  public static class BookmarkRoi implements Cloneable {

    private GeoPos upperLeft;
    private GeoPos upperRight;
    private GeoPos lowerRight;
    private GeoPos lowerLeft;

    /**
     * Creates a new BookmarkRoi.
     *
     * @param upperLeft  the upper left corner
     * @param upperRight the upper right corner
     * @param lowerRight the lower right corner
     * @param lowerLeft  the lower left corner
     */
    public BookmarkRoi(GeoPos upperLeft, GeoPos upperRight, GeoPos lowerRight, GeoPos lowerLeft) {
      this.upperLeft = upperLeft;
      this.upperRight = upperRight;
      this.lowerRight = lowerRight;
      this.lowerLeft = lowerLeft;
    }

    /**
     * Returns the upper left corner.
     *
     * @return the upper left corner
     */
    public GeoPos getUpperLeft() {
      return upperLeft;
    }

    /**
     * Returns the upper right corner.
     *
     * @return the upper right corner
     */
    public GeoPos getUpperRight() {
      return upperRight;
    }

    /**
     * Returns the lower right corner.
     *
     * @return the lower right corner
     */
    public GeoPos getLowerRight() {
      return lowerRight;
    }

    /**
     * Returns the lower left corner.
     *
     * @return the lower left corner
     */
    public GeoPos getLowerLeft() {
      return lowerLeft;
    }

    @Override
    public String toString() {
      return String.format("ROI{%s, %s, %s, %s}", upperLeft, upperRight, lowerRight, lowerLeft);
    }

    @Override
    public BookmarkRoi clone() {
      try {
        BookmarkRoi clone = (BookmarkRoi) super.clone();
        clone.upperLeft = new GeoPos(upperLeft.getLat(), upperLeft.getLon());
        clone.upperRight = new GeoPos(upperRight.getLat(), upperRight.getLon());
        clone.lowerRight = new GeoPos(lowerRight.getLat(), lowerRight.getLon());
        clone.lowerLeft = new GeoPos(lowerLeft.getLat(), lowerLeft.getLon());
        return clone;
      } catch (CloneNotSupportedException e) {
        return new BookmarkRoi(new GeoPos(upperLeft.getLat(), upperLeft.getLon()),
            new GeoPos(upperRight.getLat(), upperRight.getLon()), new GeoPos(lowerRight.getLat(), lowerRight.getLon()),
            new GeoPos(lowerLeft.getLat(), lowerLeft.getLon()));
      }
    }
  }


}
