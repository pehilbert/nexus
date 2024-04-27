# char
## Overview
The `char` data type is used to store 8-bit ASCII characters
## Size and range
- **Size:** 1 byte
- **Range:** (as an unsigned integer) 0-255
## Usage
Character literals can be written with single quotes, and are declared with the `char` keyword. In addition to being used to store ASCII characters, they can also be used in place of [integers](./int.md), in which case their numeric code will be used. Note that characters used in these cases will only have values up to 255, and storing an integer value as a character will overflow as such.
```
char myChar = 'A'; # 'myChar' will store the character 'A', or its ASCII code: 65

# Characters as integers
int diff = 'b' - 'a'; # 'diff' will store the value 1
```