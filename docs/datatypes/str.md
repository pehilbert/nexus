# str
## Overview
The `str` data type stores a reference to an array of [characters](./char.md), used to store text data.
## Size
The memory address stored in a `str` value is **4 bytes.** The data it points to could, in theory, be any size.
## Usage
Strings are mostly used in conjunction with the `print` keyword, to print strings to standard output. A string literal can be written in double quotes, or an identifier for another string can be used in its place. Note that, because strings are references and not literal data, assigning one string variable to another will cause both variables to point to the same data. As of now, Nexus only supports reassigning string variables to other existing variables; you cannot reassign an existing string to a string literal.
```
str firstString = "first";
print firstString; # Outputs 'first'

str secondString = "second";
print secondString; # Outputs 'second'

firstString = secondString;
print firstString; # Outputs 'second' again
```