SBT packaging code.

To do:

- A sane container class for native settings for rpm, deb and windows, abstracting away the obscureness in configuring all this
- Bug: Need to call windows:package-msi twice, the first time it won't find conf files because they won't exist yet
- Set a better path for windows service wrapper logs

