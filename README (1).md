# ENCRYPTYIT — Custom File Encryption Tool

ENCRYPTYIT is a Java application for password-based encryption and decryption of folders and their files. It includes a Java Swing graphical user interface (GUI), an account login system, a custom encryption algorithm, recursive folder processing, and an activity log. A console interface is also retained.

> **Project status:** Java Swing GUI implemented and connected to the existing encryption/decryption logic. AES benchmarking and automated unit tests have not been implemented. The current GUI works with **folders**, not individual file selection.

## Table of contents
- [Features](#features)
- [Progress](#progress)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Usage](#usage)
- [How it works](#how-it-works)
- [Security notes](#security-notes)
- [Roadmap](#roadmap)
- [Team](#team)

## Features

- **Java Swing GUI:** Login, signup and dashboard screens with an application logo.
- **Account management:** Signup using a first name and surname; the GUI generates a username in the format `FirstnameSurname@encrypt`. Login verifies the saved username and password. A change-password action is available in the dashboard.
- **Password requirements for GUI signup:** At least eight characters, at least one letter and one number, exactly one special character, and no spaces.
- **Password-based authentication:** The `Keymanager` class stores derived authentication data instead of plaintext passwords, using PBKDF2 and a salt.
- **Folder selection:** Choose a folder using a Swing `JFileChooser`.
- **Recursive processing:** Encrypt or decrypt supported files in the selected folder and its subfolders.
- **Custom cipher:** A SHA-256-derived keystream cipher implemented for educational use.
- **Pluggable algorithm interface:** `Encryptionalgorithm` separates encryption operations from the file processor.
- **Wrong-password or corruption detection:** A checksum is verified before a decrypted output is written.
- **Output files saved to disk:** Encryption writes `.enc` files beside the original files; decryption writes restored files beside their encrypted versions. The current file processor deletes the source after successfully writing the output.
- **File listing and activity log:** View a recursive file list or the application log through the dashboard.
- **Console mode:** The original text-based menu remains available when launched with a non-GUI command-line argument.

## Progress

| Area | Status |
|---|---|
| File I/O (read, write, recursive folder scan) | Implemented |
| Login system (signup, username, salted password hashing, password change) | Implemented |
| Data storage (`auth.dat`, `app.log`, `.enc` files) | Implemented |
| Custom encryption and decryption | Implemented |
| Console interface | Implemented |
| Java Swing GUI (`GUI.java`) | Implemented and connected to the file processor |
| Folder chooser and dashboard actions | Implemented |
| Save encrypted/decrypted output to the selected folder | Implemented (automatic output; source files replaced) |
| Individual-file selection in the GUI | Not implemented |
| AES implementation (optional benchmarking) | Not started |
| Automated unit tests | Not present in the supplied project |
| Documentation | README updated; Javadoc coverage not independently verified |

## Architecture

The application separates its graphical interface from file processing and cryptographic operations.

```text
Main
 ├── GUI (Swing)
 │    ├── Login / signup / account actions
 │    ├── Folder selection (JFileChooser)
 │    └── Encrypt / decrypt / list / log actions
 └── Console interface

GUI / Console
      │
      ▼
 Fileprocessor ─────► Folderscanner
      │
      ├─────────────► Logger
      ├─────────────► Keymanager
      └─────────────► Encryptionalgorithm (interface)
                              ▲
                              │
                         Customcipher
```

| Class | Responsibility |
|---|---|
| `Main` | Creates the core objects and starts the GUI by default; retains console mode |
| `GUI` | Swing windows, account forms, dashboard, folder chooser and action buttons |
| `Fileprocessor` | Encrypts and decrypts files within selected folders; writes output files |
| `Folderscanner` | Finds files recursively |
| `Keymanager` | Password-based key derivation and account credential verification |
| `Logger` | Writes and reads application log entries |
| `Encryptionalgorithm` | Interface for encryption and decryption operations |
| `Customcipher` | Educational custom cipher implementation |
| `FileChooser` | Additional file-chooser-related source class retained in the project |

## Project structure

```text
Custome-File-Encryption-Tool-main/
├── build.xml
├── manifest.mf
├── gitignore
├── nbproject/
│   ├── project.xml
│   ├── project.properties
│   └── ...
├── src/
│   ├── Main.java
│   ├── GUI.java
│   ├── Fileprocessor.java
│   ├── Folderscanner.java
│   ├── Keymanager.java
│   ├── Logger.java
│   ├── Encryptionalgorithm.java
│   ├── Customcipher.java
│   ├── FileChooser.java
│   └── encryption_logo.png
└── README.md
```

The project uses the exact class and source-file capitalization shown above.

## Getting started

**Requirements:** A JDK and Apache NetBeans with Java support. The project includes NetBeans/Ant build configuration files.

### Run in NetBeans

1. Extract the project ZIP file.
2. Open NetBeans and select **File → Open Project**.
3. Select the extracted `Custome-File-Encryption-Tool-main` folder (the folder containing `build.xml` and `nbproject`).
4. Open `Source Packages` and locate `Main.java`.
5. Run the project, or run the `Main` class.
6. With no command-line arguments, the Swing GUI launches automatically.

If NetBeans is configured to pass command-line arguments, remove them or use `--gui` to open the graphical interface.

### Command-line launch

From the project folder, use the included Ant build configuration if Ant and a suitable JDK are installed:

```bash
ant run
```

`Main` opens the GUI with no arguments or with `--gui`. A different argument selects the retained console path.

## Usage

### 1. Create an account or log in

- On the signup screen, enter a first name and surname, a valid password, and its confirmation.
- The application generates a username such as `JaneSmith@encrypt`.
- Return to login and authenticate with the saved username and password.
- The dashboard includes **Change password** and **Log out** controls.

### 2. Select a folder

- On the dashboard, click **Choose folder**.
- Select the folder containing the files you want to process.
- The selected folder path is shown in the dashboard.
- Use **List files** to view the files found recursively.

### 3. Encrypt files

1. Back up the folder first.
2. Click **Encrypt folder**.
3. Confirm the warning.
4. Enter the folder encryption password.
5. The program processes files and reports a count. Review **Activity log** for errors.

Encrypted output is saved automatically in the same folder as each source file with the `.enc` extension. The original source file is deleted after the encrypted file is successfully written.

### 4. Decrypt files

1. Select the folder containing the `.enc` files.
2. Click **Decrypt folder**.
3. Confirm and enter the password originally used to encrypt those files.
4. The program verifies each decrypted file's checksum and writes restored output in the corresponding folder.

The encrypted source file is deleted after the restored output is successfully written. A wrong password or corrupted data is reported as an error.

**Important:** The GUI does not currently ask you where to save each output file. It writes to the source folder automatically. Encryption and decryption can replace files with the same output name, so use test files and keep backups.

### 5. View activity

Use **Activity log** to read the logged activity and errors.

## How it works

The encrypted file format is:

```text
[ 16-byte random salt ][ encrypted(original file bytes + 32-byte SHA-256 checksum) ]
```

1. A new random salt is generated for each encrypted file.
2. `Keymanager` derives a 256-bit key from the supplied password and salt using PBKDF2-HMAC-SHA256. The updated source sets the iteration count to **210,000**.
3. `Customcipher` encrypts the file content and its checksum.
4. During decryption, the checksum is compared with a newly calculated SHA-256 digest before the restored file is written.
5. The output is written next to the source; after successful writing, the source is deleted according to the current `Fileprocessor` setting.

## Security notes

- **Educational project only:** The custom cipher is not a vetted encryption scheme and should not be trusted to protect sensitive or irreplaceable information. A standard authenticated encryption scheme such as AES-GCM is preferable for real-world use.
- **Back up your files:** The current processor deletes original files after writing the output. Do not test with your only copy of important data.
- **Whole-file processing:** The processor uses `Files.readAllBytes`, so very large files may exhaust available memory.
- **Keep private runtime files out of version control:** Protect `auth.dat`, `app.log`, and encrypted or sample data as appropriate.
- **Checksum limitation:** A SHA-256 checksum appended before encryption detects accidental corruption or an incorrect password in ordinary cases, but it is not a substitute for authenticated encryption against deliberate tampering.
- **Testing status:** Source review confirms implemented code paths, but does not establish that every feature has passed runtime testing.

## Roadmap

- [x] Implement Java Swing login and signup screens
- [x] Implement the file encryption dashboard
- [x] Connect the GUI to folder encryption and decryption
- [x] Select folders using `JFileChooser`
- [x] Save processed files automatically to disk
- [x] Display file lists and the activity log
- [ ] Add single-file selection and processing to the GUI
- [ ] Add an option to keep original files instead of replacing them
- [ ] Implement an AES-GCM algorithm and optional benchmarking
- [ ] Add unit tests for authentication, cipher operations, and file processing
- [ ] Stream large files instead of loading them fully into memory
- [ ] Test GUI and file operations thoroughly on supported platforms

## Team

- Abraham Moteane
- Isaac Khwesa
- Victoria Mangira
- Zachuma Ngculana
- Uwase Opportunee
- Asithandile Magqabi
- Libhongo Msolo
