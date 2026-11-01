
private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {

    // Get the username and password
    String username = txtUsername.getText();

    String password = new String(txtPassword.getPassword());

    // Check whether the fields are empty
    if (username.isEmpty() || password.isEmpty()) {

        JOptionPane.showMessageDialog(this,
                "Please enter your username and password.");

        return;
    }

    // Check the saved login details
    try {

        File file = new File("users.txt");

        Scanner scanner = new Scanner(file);

        boolean loginSuccessful = false;

        while (scanner.hasNextLine()) {

            String line = scanner.nextLine();

            String[] details = line.split(",");

            // Compare the entered details with saved details
            if (details.length == 2 &&
                details[0].equals(username) &&
                details[1].equals(password)) {

                loginSuccessful = true;
                break;
            }
        }

        scanner.close();

        // Check whether login was successful
        if (loginSuccessful) {

            JOptionPane.showMessageDialog(this,
                    "Login successful!");

            // Open the main application window
            MainPage main = new MainPage();

            main.setVisible(true);

            this.dispose();

        } else {

            JOptionPane.showMessageDialog(this,
                    "Incorrect username or password! Please enter correct username or password.");
        }

    } catch (FileNotFoundException e) {

        JOptionPane.showMessageDialog(this,
                "No registered accounts found.");
    }
}
