# str
## Overview
The `str` data type stores a string of characters that can be used for printing. Note that strings in Nexus are their own data type and are immutable, thus differentiating them from a simple array of characters.
## Usage
Strings are mostly used in conjunction with the [`print`](../keywords/print.md) keyword, to print strings to standard output. A string literal can be written in double quotes, or an identifier for another string can be used in its place. String variables can be declared with the `str` keyword.
```
str helloString = "Hello, world!"; # defines a string with a string literal
print helloString; # prints the string
print "\n";
```