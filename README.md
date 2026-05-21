An example of REST API integration with data encryption on both sides.

The client and server share the same secret key and algorithm: AES/GCM/NoPadding. 

1. The Client sends an encrypted Order as a Body with the encrypted password and Client ID in the Headers.

2. The Server receives the Request and sends a 202 confirmation without the Body, with only the Header containing the generated Order ID.

3. The Server starts an asynchronous thread and decrypts the Body and password using the same algorithm as the Client.

4. The Server verifies the Client's decrypted password by comparing it with the password stored in its resources assigned to the given Client ID.

5. After positive verification and successful decryption of the Order from the Request Body, the Order is saved by the Server.

6. After the Server successfully saves the Order, the encrypted Order is sent as a Request Body to the Client with the status "Processed."

7. The Client receives the Request with the status "Processed" and decrypts the Body using the same algorithm as the Server.
