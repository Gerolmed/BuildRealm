# Drafts
Every player can have drafts. Drafts represent a personal Slime world. Via the GUI they can invite other players to collaborate.

# Publishing
> **NOTE:** Currently categories are hardcoded. Support for custom categories will be the next priority!

When a world is finished a player may publish it to a group and a category. After that a world can not be edited directly
anymore. Published worlds can be forked which will create a draft with a clone of the original world. When publishing a
fork you have the option to overwrite the parent, make it a variation or a completely new instance. It's possible to break
the connection at any time, but that can't be undone.

# Exporting
While there is an export mechanism, which will create Schematics the sizes are currently hardcoded leading to this feature
being quite useless rn. Customizable bounds will probably be added later,


## Commands:
 - `/draft list` Shows all your drafts and drafts you are a collaborator in
 - `/draft list {player}` Shows all drafts of a player and drafts the player is a collaborator in
 - `/draft create` Create a new draft (if personal limit not exceeded)
 - `/draft leave` Leave the draft you are currently in

 - `/group create {groupId}` Create a new group
 - `/group list` Show all groups you can see
 - `/group open {groupId}` Opens a specific group
