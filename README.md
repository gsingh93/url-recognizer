URL Recognizer
==============

An MHacks 2013 hack by Gulshan Singh and Aaron Dimet.

This Android app allows you to take a picture with your phone and go to any URL in the image.

Building
--------

To build, clone the project, and run `git submodule update --init --recursive` in the root directory of the project. This will clone the [tess-two](https://github.com/rmtheis/tess-two) library into `contrib/tess`. This is an Android port of the open source OCR [Tesseract](http://code.google.com/p/tesseract-ocr/). To build this library, cd into `contrib/tess/tess-two` and run the following commands,

```
ndk-build
android update project --path .
ant release
```

Note that means that the Android SDK tools and the Android NDK tools must be in your path and you must have Apache Ant installed.

Then import the project as a library into eclipse. Right click on your project, select Properties -> Android, click Add and select the tess-two project. The application should then build without errors.
