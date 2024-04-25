# int
## Overview
The `int` data type is used to store 32-bit signed integers.
## Size and range
- **Size:** 4 bytes
- **Range:** -2,147,483,648 to 2,147,483,647
## Usage
Integer literals can be written as a sequence of digits, optionally preceded by a negative sign for negative numbers. When declaring an int variable, the identifier is preceded by the `int` keyword.

```
int a = 42; # Declares an integer variable 'a' with a value of 42.
```
Integer expressions may also be built using the four common operators: addition, subtraction, multiplication, and division. Note that division with integers will always round down to the nearest whole number. These expressions are evaluated with standard precedence: multiplication and division, then addition and subtraction. Expressions may also use parentheses, which will cause the nested expression to be evaluated before anything else.
```
int a = 9 / 2; # 'a' will store the value 4
int b = (3 - 1) * (a + 6); # 'b' will store the value 20, coming from 2 * 10
```