## About

`opengles2-2d-demos` is a simple android 4.0 application that showcases some simple 2d examples in OpenGL ES 2.0. This project serve as a sample for how to effectively use OpenGL ES 2.0 for a 2d application (ex: a game) and to track my personal progress on the subject. All samples are made in the most legible possible way I could find and are full of comments explaining the important stuff.

Be warned that **every** file is standalone - this generate a lot of code duplication, *BUT* it's slightly easier to follow (you only need to look to the file that shows what you're interested, no need to "hunt" initialization routines, etc.). In short, don't use this project structure to do anything serious or you will probably be murdered by your manager and/or teammates.

## The Samples

Every sample can be accessed from the initial activity. The available samples are:

1. **Epilepsy**: The most basic sample, shows how to initialize OpenGL ES 2.0. Every frame, the background is changed to a random color. Useful for testing if you or your friends have epilepsy.
2. **Triangle2d**: Shows how to setup the coordinate system and draw a triangle in a fixed position. A must-see for anyone trying to work with 2D in Open GL ES 2.0.

## License
BSD. Basically, you're free to do whathever you like with this code.
