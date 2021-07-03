# BruhBotLib
Library for creating BruhBot modules.

## What is this?
BruhBot is designed to work in a modular fashion. The user runs the main bot JAR, then executes different bot modules separately and links them to the bot via the `module load X` command. This is the library that modules must implement.


## Contents

- `BBModule` interface that factories must implement for RMI operation.
- `Command` abstract class that all bot commands must inherit from.
- And more to come!
