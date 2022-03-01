## DraftCommand

Permissions ending with `.other` always include `.own`.

These are the ranks our build server has:
ADMIN;DEV;BUILDER;CONTENT;MODERATION;JRBUILDER;PLAYER;ACCEPTED

All permissions have a note which role has what permissions on our end. So adjust it to match with yours.

### Theme
 - build_realm.group.create = Allows creating a group     `ADMIN`
 - build_realm.group.open.all = Allows opening and see all groups  `ADMIN;DEV;BUILDER;CONTENT`
 - build_realm.group.open.{groupName} = Allows opening and see a specific group `MODERATION;JRBUILDER;PLAYER`
 - build_realm.group.list = Allows opening group list gui   `ADMIN;DEV;BUILDER;CONTENT`
 - build_realm.group.see.all = Allows to see all lists (does not allow opening!)     `ADMIN;DEV;BUILDER;CONTENT wird nicht gebraucht wegen open.all`
 - build_realm.group.edit.all = Allows to edit all group settings   `ADMIN`
 - build_realm.group.edit.{groupName} = Allows editing group settings of a specific group       `ADMIN`
 - build_realm.group.delete.all = Allows deleting groups      `ADMIN`
 - build_realm.group.delete.{groupName} = Allows to delete a specific group      `ADMIN`

### Draft
 - build_realm.draft.create = Allows creating drafts `ADMIN;DEV;BUILDER;CONTENT;MODERATION;JRBUILDER;PLAYER`
 - build_realm.draft.max.{amount} = Allows creating {amount} drafts. (The number has to be set in the `config.yml`)   `DEV;BUILDER;CONTENT;MODERATION;JRBUILDER;PLAYER`
 - build_realm.draft.max.infinite = Allows creating infinite drafts.   `ADMIN`

 - build_realm.draft.list.own = Allows to see your own drafts    `ADMIN;DEV;BUILDER;CONTENT;MODERATION;JRBUILDER;PLAYER`
 - build_realm.draft.list.other = Allows seeing drafts of any other player `ADMIN;DEV;BUILDER`
 - build_realm.draft.view.other = Allows visiting other players drafts `ADMIN;DEV;BUILDER`

 - build_realm.draft.edit.other = Allows building and use world edit on other drafts (when not set as collaborator)  `ADMIN`
 - build_realm.draft.floating = Allows to see all floating drafts (drafts who had their group removed)    `ADMIN`

 - build_realm.draft.leave = Allows leaving drafts via the leave command or by trying to visit another draft    `ADMIN;DEV;BUILDER;CONTENT;MODERATION;JRBUILDER;PLAYER`

**GUI:**
 - build_realm.draft.delete.other = Allows to delete other users drafts   `ADMIN`
 - build_realm.draft.publish.own = Allows to publish your own drafts   `ADMIN`
 - build_realm.draft.publish.other = Allows publishing other users drafts   `ADMIN`
 - build_realm.draft.manage_members.own = Allows viewing member management UI of your own drafts     `ADMIN;DEV;BUILDER;CONTENT;MODERATION;JRBUILDER;PLAYER`
 - build_realm.draft.manage_members.other = Allows viewing member management UI of other players drafts   `ADMIN`
 - build_realm.draft.manage_members.add.own = Allows adding collaborators to your own drafts    `ADMIN;DEV;BUILDER;CONTENT;MODERATION;JRBUILDER;PLAYER`
 - build_realm.draft.manage_members.add.other = Allows adding collaborators to other users drafts   `ADMIN`
 - build_realm.draft.toogle.own = Allows changing "draft rank" of collaborators in your drafts   `ADMIN`
 - build_realm.draft.toogle.other = Allows changing "draft rank" of collaborators in other players drafts   `ADMIN`

 - build_realm.draft.unfork.other = Allows detaching forks of other users   `ADMIN`

### Piece
 - build_realm.piece.view.all = Allows viewing published worlds `ADMIN;DEV;BUILDER;CONTENT`
 - build_realm.piece.view.{groupName} = Allows viewing published worlds of a specific group  `ADMIN`
 - build_realm.piece.delete.all = Allows deleting published worlds `ADMIN`
 - build_realm.piece.delete.{groupName} = Allows deleting published worlds of a specific group  `ADMIN`
 - build_realm.piece.fork.all = Allows forking/clone all published worlds `ADMIN;DEV;BUILDER;CONTENT;MODERATION;JRBUILDER;PLAYER`
 - build_realm.piece.fork.{groupName} = Allows forking/clone published worlds of a specific group `ADMIN`

### Publish
 - build_realm.publish.any = Allows to publish all types of worlds (original/original new/variant)   `ADMIN`
 - build_realm.publish.original = Allows to replace a world (only possible if forked)  `ADMIN`
 - build_realm.publish.original.{groupName} =  Allows to replace a world in a specific group      `ADMIN`
 - build_realm.publish.variant = Allows to publish a variant      `ADMIN`
 - build_realm.publish.variant.{groupName} = Allows to publish a variant for a specific group    `ADMIN`
 - build_realm.publish.original_new = Allows to publish a new world in the group of the forked parent      `ADMIN`
 - build_realm.publish.original_new.{groupName} = Allows to publish a new world in the group of the forked parent for specific groups   `ADMIN`
 - build_realm.publish.new = Allows to publish a new world to all groups   `ADMIN`
 - build_realm.publish.new.{groupName} = Allows to publish a new world to a specific group     `ADMIN`
