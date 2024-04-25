# exit

## Overview
Exits the program with a certain numeric code.

## Syntax
```exit <int_expression>;``` will exit the program with the numeric code given by `<int_expression>`.

## Example Usage
```
# Some successful code

# Exiting with code 0 indicates success
exit 0;
```
```
# Some code with an error

# Exiting with a nonzero value indicates some kind of failure.
exit 1;
```