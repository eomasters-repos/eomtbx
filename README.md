EOMasters Toolbox for SNAP
===============================
[![Static Badge](https://img.shields.io/badge/%F0%9F%8C%90-eo?style=for-the-badge&logoSize=auto&label=EOMasters&color=262626)](https://www.eomasters.org)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/company/eomasters)
[![Bluesky](https://img.shields.io/badge/Bluesky-0285FF?style=for-the-badge&logo=bluesky&logoColor=fff&labelColor=0285FF)](https://bsky.app/profile/eomasters.org)
[![Mastodon](https://img.shields.io/badge/Mastodon-6364FF?style=for-the-badge&logo=Mastodon&logoColor=white)](https://mastodon.green/@EOMasters)
[![ko-fi](https://img.shields.io/badge/Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/eomasters)
[![Maven Build](https://github.com/eomasters-repos/eomtbx/actions/workflows/mvn-build.yml/badge.svg)](https://github.com/eomasters-repos/eomtbx/actions/workflows/mvn-build.yml)
[![Static Badge](https://img.shields.io/badge/FOR%20ESA%20SNAP-Version%2014-00AA89?labelColor=5A5A5A)](https://step.esa.int/main/)

<div align="center">
I'm proud to have been awarded a grant from the German “Bescheinigungsstelle Forschungszulage” — which roughly<br>
translates to “Research Grant Certification Office” or something along those lines. I am now allowed to wear this badge.
  <img src="BSFZ_Siegel_RGB.png" alt="BSFZ_Siegel_RGB.pn" width="300">
</div>

---
The EOMasters Toolbox (EOMTBX) is a collection of tools, processors, readers, and workflow enhancements that save time
while working with ESA's SNAP.
<div align="center">
  <img src="src/main/resources/org/eomasters/eomtbx/icons/eomtbx_logo.svg" alt="EOMTBX logo" width="180">
</div>

The toolbox comprises, for example, the following tools, features and options:

* Quick Menu<br>
  Provides quick access to the most often used menu actions.
* Loop GPT<br>
  Executes several GPT commands in one go.
* Band Maths Extensions<br>
  Adds new functionalities to Band Maths, like window calculations and checking if pixels are invalid.
* Wavelength Editor<br>
  Allows editing the wavelength properties of multiple bands and applying the changes to compatible products.
* Asset Library<br>
  Stores frequently used sites, geometries, masks, and other resources for quick access.
* SpeX - Spectral Index Database<br>
  Provides hundreds of spectral indices, executes them, and supports custom definitions.
* PyEditor and SnapKit<br>
  Edit and execute Python scripts directly in SNAP.
* Coastal Map and Cyanobacteria Index processors
* Sentinel-2 geometry up-scaling
* Sentinel-2 5 m super-resolution
* Sentinel-2 surface reflectance normalisation
* Sentinel-2 620 nm band estimation over water
* Sentinel-2 MSI L2A reader fixing issue of B1 detector footprint
* ASTER L1T and EMIT L1B/L2A readers
* Close Product Without a View<br>
  Simply closes all products that are not used by a Scene View.
* Generate System Report<br>
  Generates a system report usable for error reports to the developers.
* Options<br>
  Allow changing the settings of the tools and exporting them into a separate file, which can later be imported.

## Feedback

If you have suggestions for improvements and extensions, please use the
[issue tracker](https://github.com/eomasters-repos/eomtbx-issues/issues) or post on the [EOMasters Groups](https://www.eomasters.org/groups).

## Release Notes

The release notes can be found [here](https://github.com/eomasters-repos/eomtbx/releases).
