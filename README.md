# Custom File Encryption Tool

A Java tool that encrypts and decrypts whole folders of files using a password. It uses a custom-built cipher behind a swappable interface, a master-password login, and an activity log.

> **Project status: Console prototype (milestone 1).** The core logic and the console interface work. The GUI is planned for the final milestone.

---

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

---

## Features
- **Login system:** a master password is created on first run. Only a salt and a PBKDF2 hash are stored, never the password itself. Three attempts are allowed.
- **Folder encryption and decryption:** processes a folder and all of its sub-folders recursively.
- **Custom cipher:** a SHA-256 based stream cipher implemented from scratch.
- **Pluggable algorithms:** all ciphers implement the `EncryptionAlgorithm` interface, so another algorithm (e.g. AES) can be added without changing the core.
- **Wrong-password detection:** a SHA-256 checksum is stored with every file, so a wrong password or a damaged file is detected and nothing is overwritten.
- **Activity log:** every action and error is written to `app.log` with a timestamp.
- **Safe replacement:** an original file is deleted only after its encrypted copy has been written successfully.

## Progress

| Area | Status |
|---|---|
| File I/O (read, write, recursive folder scan) | Done |
| Login system (master password, hashed and salted) | Done |
| Data storage (`auth.dat`, `app.log`, `.enc` files) | Done |
| Basic processing (encrypt / decrypt) | Done |
| Console interface | Done |
| Code documentation (Javadoc on classes and methods) | Done |
| GUI | Placeholder only (`GUI.java`) |
| AES implementation (optional, for benchmarking) | Not started |
| Unit tests | Not started |

## Architecture

The design follows our UML class diagram. The user interface only collects input and shows results. All real work happens in the core classes.

```
Main ──► GUI (placeholder) ──► FileProcessor ──► FolderScanner
                                    │ ├────────► Logger
                                    │ ├────────► KeyManager
                                    │ └────────► <<interface>> EncryptionAlgorithm
                                    │                      ▲
                                    │                      └── CustomCipher
```

| Class | Responsibility |
|---|---|
| `Main` | Entry point: builds the objects, runs login, shows the console menu |
| `GUI` | Placeholder for the future graphical interface |
| `FileProcessor` | Coordinates encrypting and decrypting folders and files |
| `FolderScanner` | Recursively finds all files in a folder |
| `KeyManager` | Derives keys from passwords (PBKDF2) and handles master-password verification |
| `Logger` | Writes timestamped events and errors to `app.log` |
| `EncryptionAlgorithm` | Interface: `encrypt(data, key)` and `decrypt(data, key)` |
| `CustomCipher` | Our implementation of `EncryptionAlgorithm` |

## Project structure

```
.
├── Main.java
├── GUI.java
├── FileProcessor.java
├── FolderScanner.java
├── KeyManager.java
├── Logger.java
├── EncryptionAlgorithm.java
├── CustomCipher.java
└── README.md
```
> Adjust this tree if your sources live in a folder such as `src/`.

## Getting started

**Requirements:** Java 11 or newer (JDK).

```bash
# clone
git clone <your-repository-url>
cd <repository-folder>

# compile
javac *.java

# run the console version
java Main
```

Using NetBeans or another IDE: open the project and run `Main`. Inside an IDE the password prompt is visible because IDEs have no real console.

## Usage

1. **First run:** create a master password (at least 6 characters). Later runs ask for it.
2. Choose from the menu:

```
1) Create sample files (demo_data)
2) List files in a folder
3) Encrypt a folder
4) Decrypt a folder
5) View activity log
6) Exit
```

**Quick demo:** choose `1` to create sample files, then `3` to encrypt `demo_data`, and `2` to see the `.enc` files. Try `4` with a wrong password (every file is rejected and left untouched), then with the right password (files are restored). Finally choose `5` to see the log.

Files created while running: `auth.dat` (master-password salt and hash), `app.log` (activity history), `demo_data/` (sample files).

## How it works

Each encrypted file (`name.ext.enc`) has this layout:

```
[ 16-byte random salt ][ cipher( original bytes + 32-byte SHA-256 checksum ) ]
```

1. A fresh random salt is generated for every file.
2. `KeyManager` derives a 256-bit key from the password and salt using PBKDF2-HMAC-SHA256 (100,000 iterations).
3. `CustomCipher` XORs the data with a keystream made by hashing the key together with a block counter using SHA-256.
4. When decrypting, the checksum is verified before anything is written. If it does not match, the password is wrong or the file is damaged.

## Security notes

- `CustomCipher` is **a learning exercise**. Home-made ciphers are not safe for protecting real secrets. A vetted algorithm such as AES-GCM should be used in practice, and the `EncryptionAlgorithm` interface makes swapping it in simple.
- Do not use this tool on the only copy of important files.
- Never commit `auth.dat`, `app.log`, or sample data to the repository (see `.gitignore`).
- Whole files are read into memory, so very large files are not supported yet.

## Roadmap
- [ ] Implement the GUI (Swing) and connect it to `FileProcessor`
- [ ] Add `AESCipher` as an optional algorithm and benchmark it against `CustomCipher`
- [ ] Stream large files instead of loading them fully into memory
- [ ] Add unit tests for the cipher, key manager and file processor
- [ ] Option to keep original files instead of replacing them

## Team
| Names | 
| Abraham Moteane | 
| Isaac Khwesa |
| Victoria Mangira |
| Zachuma Nculana|
| Uwase Opportunee|
| Zachuma Ngculana|


