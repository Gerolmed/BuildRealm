# Build Realm

**This project has not been fully released yet and is still under development in some areas**

1. [Support](#support)
1. [About](#about)
1. [Dependencies and Libraries](#dependencies-and-libraries)
1. [History](#history)
1. [For Developers](#for-developers)
1. [Special Thanks](#special-thanks)


## Support
Any issues or questions? Need help with the setup. You can reach us on our discord.

[<img src="https://discordapp.com/assets/e4923594e694a21542a489471ecffa50.svg" alt="" height="55" />](https://endrealm.net/discord)

## About
> First of: This is not a replacement for multiverse (SWM isn't either).
> The goal of this system is to **optimize and organize** the world creation flow
> **when creating SWM worlds**. It is not possible to convert SWM worlds to normal world
> as for current state of development. Due to using slime worlds all limitations of slime
> worlds apply here as well (Max world size, ...). For more information checkout the SWM 
> page linked under dependencies.

BuildRealm was made to optimize and organize world building on e.g. build servers. It allows players
to create drafts, collaborate and publish them. It also allows creating variations of existing builds
by giving the users the option to fork published worlds. Through a large set of permissions it is possible
to exactly control, what each user is allowed to do.

**Key functionalities:**
 - player drafts
 - draft collaboration
 - world forking
 - world variations
 - published world grouping and author tracking
 - rich permission set

## Dependencies and Libraries
**You need to have slime world manager installed on you server for this to work**
- (**Recommended**): _https://github.com/Paul19988/Advanced-Slime-World-Manager_
- (Original): _https://github.com/Grinderwolf/Slime-World-Manager_

For the inventory guis we use a library called [SmartInvs](https://github.com/MinusKube/SmartInvs). This library is included in the project and does **not** have to be downloaded seperatly.

## History

This project was initially created to manage the different dungeon pieces of the EndRealm LostSouls rogue like gamemode.
Different Pieces like Rooms and Paths where grouped into themes. Many paths and rooms ended up having a lot of variations.
We needed a way of organizing the build process, cause the numeration of rooms was getting more and more complex.

If you want to know why the code has some very ugly synchronization code... I just wanted to see if there are any
benefits in performance, when running as much as possible async. Turns out mostly just makes stuff complicated :P

## For Developers
If you have a feature or new function implemented, feel free to setup a [PullRequest](https://github.com/Gerolmed/BuildRealm/pulls).

## Special thanks
Thanks to all the people maintaining SWM it's a great system and I hope we can make it more popular.
