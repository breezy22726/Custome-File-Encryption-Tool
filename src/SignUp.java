
private void btnSignUpActionPerformed(java.awt.event.ActionEvent evt) {

    // Get the username entered by the user
    String username = txtUsername.getText();

    // Get the password entered by the user
    String password = new String(txtPassword.getPassword());

    // Check if the fields are empty
    if (username.isEmpty() || password.isEmpty()) {

        JOptionPane.showMessageDialog(this,
                "Please fill in all fields.");

        return;
    }

    // Check if the username already exists
    try {

        File file = new File("users.txt");

        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {

            String line = scanner.nextLine();

            String[] details = line.split(",");

            if (details[0].equals(username)) {

                JOptionPane.showMessageDialog(this,
                        "Username already exists. Please enter new username.");

                scanner.close();
                return;
            }
        }

        scanner.close();

    } catch (FileNotFoundException e) {

        // File does not exist yet, so a new one will be created automatically by Java.
    }

    // Save the new username and password
    try {

        FileWriter writer = new FileWriter("users.txt", true);

        writer.write(username + "," + password + "\n");

        writer.close();

        JOptionPane.showMessageDialog(this,
                "Account created successfully!");

        // Clear the fields
        txtUsername.setText("");
        txtPassword.setText("");

    } catch (IOException e) {

        JOptionPane.showMessageDialog(this,
                "Error creating account.");
    }
}
