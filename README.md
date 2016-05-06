# Kanto Wallpaper
An Android Live Wallpaper that scrolls through the different regions of Kanto in Pokemon.

Supports Ice Cream Sandwich and above (could probably even make it lower, and support all 10 people who have an older android device than that). It's only been tested on my Vivo 5 (Android Lolipop) device, so let me know it something doesn't work for your device.

Check the Releases Tab to download the current APK.

## Regions

The wallpaper scrolls around these regions: 
- Pallet Town
- Veridian City
- Pewter City
- Cerulean City
- Vermilion City
- Lavender City (Small town, so it only has two waypoints. Needs improvement)
- Celadon City
- Fuchsia City
- Cinnabar Island (Doesn't even move because the island is so small. Maybe remove?)

I might add a few routes in there soon too. 

## Preview

I made a gif preview. I don't know how to make a well optimized gif, so it looks a lot faster and smoother on device :(

![Preview](./preview.gif)

## Customizing

If you go into the apps <b>assets</b> folder, you'll see a bunch on images and a json file. 

The json file contains an array of areas that can be used as the wallpaper, and defines a few values:

<b>id</b>: The id of the wallpaper. 

<b>image</b>: The image from the assets folder to use.

<b>viewport</b>: The size (in pixels) on the viewport that the device will scale to fit the screen. If the phone is in portrait, the vertical height will contain the number of pixels defined as the viewport, and the width will be the <i>viewport * the aspect ratio of the device</i>.

<b>speed</b>: Used to make the wallpaper scroll faster or slower. Defined as pixels per frame. Wallpapers with a smaller viewport will seem to go faster than one with a larger viewport, since the speed is in pixels, and a smaller viewport is showing fewer pixels from the picture at any given moment.

<b>nodes</b>: An array of coordinates to scroll to. Keep in mind that if your viewport is defined as 100, a top-left node should be { 50, 50 }, since { 0, 0 } would try to show outside the bounds of the image since the viewport is centered on the node. For variety in wallpapers, the beginning node is randomly selected, and the node order is reversed 50% of the time. 
