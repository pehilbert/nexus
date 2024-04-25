# Welcome to Nexus
Nexus is a work-in-progress (WIP) general-purpose programming language designed to bridge the gap between high-level convenience and low-level power. At its core, Nexus aims to offer a unique synthesis of accessibility and control, allowing programmers access to convenient high-level functionalities, while still giving them the option to delve into low-level features if they choose.

## Core Philosophy
The philosophy behind Nexus is to provide robust low-level building blocks as the foundation of the language. These foundational elements are designed to support the development of comprehensive high-level libraries. This approach allows programmers to:

- Utilize existing high-level libraries for rapid application development and enhanced productivity.
- Engage directly with the base language for fine-grained control and optimization.
- Extend the ecosystem by creating and sharing their own libraries, fostering a vibrant community of contributors.

##  Vision
My vision for Nexus is to cultivate a versatile programming environment where the community can drive innovation and growth. By offering tools that range from direct hardware manipulation to high-level functional operations, Nexus empowers developers to craft solutions that are both elegant and efficient.

# Getting Started
This section will guide you through the necessary steps to get Nexus up and running on your system.

## Prerequisites
Before you begin, ensure you meet the following requirements:

- **Operating System:** A Linux 32-bit system is required to run Nexus effectively.
- **Java Development Kit (JDK):** Nexus is implemented in Java, so you will need to have the Java Development Kit installed. You can download it from Oracle's official website or install it via your package manager.

## Installation Steps
1. **Clone the Repository:**<br>
First, clone the Nexus repository to your local machine using Git:
```bash
git clone https://github.com/yourusername/nexus.git
cd nexus
```
2. **Set Up the Environment:**<br>
Add the Nexus directory to your PATH to make the nexc compiler and nexe script accessible from anywhere on your system. You can do this by adding the following line to your ~/.bashrc or ~/.bash_profile file:
```bash
export PATH="$PATH:/path/to/nexus"
```
After editing the file, apply the changes by running:
```bash
source ~/.bashrc
```
3. **Verify the Installation:**
Write a simple Nexus program to test the installation. If you are in the `nexus` directory, you can create a new directory called `testing` and it will be ignored by Git. However, you can create Nexus source code files from anywhere. In the directory of your choice, create a file named `test.nex` with the following content:
```
exit 0;
```
Compile and run your program using the nexc command:
```bash
nexc test.nex test
```
To view the exit code of your program, you can use the nexe script:
```bash
nexe ./test
```
If all went well, you should see the following output:
```
Process finished with code: 0
```
Alternatively, you can execute the compiled program normally to see its behavior without specifically checking the exit code.