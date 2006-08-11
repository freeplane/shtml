SimplyHTML readme file
stage 11, April 27, 2003

Copyright (c) 2002, 2003 Ulrich Hilger 
http://www.lightdev.com
(see 'License' below)

This file contains
------------------
  About SimplyHTML
  Contents
  Installation
  Requirements
  Usage
  License

About SimplyHTML
----------------
  SimplyHTML is an application for text processing. It
  stores documents as HTML files in combination with
  Cascading Style Sheets (CSS).

  SimplyHTML is not intended to be used as an editor for
  web pages. The application combines text processing
  features as known from popular word processors with a
  simple and generic way of storing textual information
  and styles.

Installation
------------
  Once downloaded

    1. create an arbitrary folder such as 
        'c:\Programs\SimplyHTML'
    2. extract contents of the downloaded zip file into
        that folder

  Contents of the downloaded zip file can be restored
  by using one of the many applications capable to
  extract ZIP files. If you do not have such an
  application, you can use application Extractor
  available free at

      http://www.calcom.de/eng/product/xtract.htm

Contents
--------
  The distribution package comes as a single compressed
  zip file. It contains

    SimplyHTML.jar........the executable file for the
                            latest stage of SimplyHTML
    Help.pdf..............tutorial as PDF file
    readme.txt............this file
    gpl.txt...............the file containing the license
                            agreement valid for all parts
                            of the distribution package
    jhall.jar.............JavaHelp runtime extension
    source................source code directory
    doc...................directory with API documentation
                            files

  Please refer to 'License' below for terms and
  conditions of using above parts.

Requirements
------------
  To compile and run the sources, a Java 2 Standard
  Edition 1.4 (J2SE) or higher Software Development Kit (SDK)
  is required.

  To only run the executable JAR file or the classes of
  SimplyHTML, a Java Runtime Environment (JRE) is required.

  J2SE and/or JRE can be obtained at http://java.sun.com/j2se/1.4

Usage
-----
  The javadoc API documents can be read with any web browser.
  To read the javadoc API documents, open file 'index.htm' in
  the doc directory from out of your browser.

  The source files can be viewed with any text editor.
  Please refer to the documentation of the J2SE SDK about how
  to compile source files to classes and how to run classes.

  To run the executable JAR file, use the following command
  on the command line prompt of your system
   (replace \ by / and omit .exe on Unix or Linux systems)

    [JRE]\bin\javaw.exe -jar [AppDir]\SimplyHTML.jar

  [AppDir] in above command means the directory, you have
  installed SimplyHTML on your computer. [JRE] means the
  directory, the  Java Runtime Environment (JRE) is stored on
  your computer.

  NOTE: All paths should not contain blanks. A path such
  as C:\Program Files\SimplyHTML as the <AppDir> will
  only work if it is put in quotes, such as in
  "C:\Program Files\SimplyHTML\SimplyHTML.jar"

License
-------
  This distribution of SimplyHTML is published under the terms
  and conditions of the GNU General Public License. To read the
  license, please open file 'gpl.txt' which is part of this
  package too.
