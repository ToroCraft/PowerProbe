
# Redstone Power Probe

This Minecraft Mod adds a new item to the game, the Redstone Power Probe.  With the power probe in hand, a player will see information about the current redstone power level of the block under their cursor.  The block will be shaded red if the block is strongly powered, yellow if the block is weakly powered or not altered at all for unpowered blocks.  When blocks are powered a number to be visible to the right of the cursor.  This number represents the current power level of the block which can be 1 to 15.

## Redstone Power Probe Recipe

![Redstone Power Probe Recipe](http://i.imgur.com/lcSWFfH.png)

## Probe Powered Block

![Powering](http://i.imgur.com/RcetkN4.png)

Right clicking with the wand in hand will cause the power probe to power the block under the cursor.  The power probe can only power blocks on faces that are adjacent to an air block.  When the probe is powering a block, a small redstone block will be visible on the face of the block it is strongly powering.

## Strongly Powered Overlay

![Strongly Powered Overlay](http://i.imgur.com/5VIIcsx.png)

## Weakly Powered Overlay

![Weakly Powered Overlay](http://i.imgur.com/PztQoe6.png)

## Development Environment Setup
Download the desired version of Forge MDK from https://files.minecraftforge.net/ and unzip the MDK into a new directory. After the MDK is unzipped, clone this repository into the `src` directory as `main`. Then you will need to either copy or link the `build.gradle` from the repository to the root of the MDK, replacing the original one. 

### Setup Example
Replace `<MDK_FILE>` with the file name of the MDK you downloaded (for example `forge-1.11.2-13.20.0.2228-mdk.zip`)

```
mkdir ~/PowerProbe
cd ~/PowerProbe
cp <MDK_FILE> .
unzip <MDK_FILE>
rm -rf src/main
git clone git@github.com:ToroCraft/PowerProbe.git src/main
mv build.gradle build.default.gradle
ln -s src/main/build.gradle build.gradle
./gradlew setupDecompWorkspace
./gradlew eclipse
```

