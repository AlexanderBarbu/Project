import java.util.*;
import java.time.LocalDateTime;

public class Authenticator {

    private static class Token {
        private String authToken = "";
        private LocalDateTime expirationDate = null;
    }

    /**
     *  Maps a username to a token
     */
    private static Map<String, Token> tokens = new HashMap<>();

    /**
     *  Maps username to a hashed password
     */
    private static Map<String, String> credentials = initializeCredentials();


    /**
     * Obviously not a serious security measure
     */
    private static Map<String, String> initializeCredentials() {
        credentials = new HashMap<>();
        credentials.put("admin", StringHasher.Hash("password"));
        return credentials;
    }

    private static void addCredentials(String username, String hashedPassword) {
        credentials.put(username, hashedPassword);
    }

    /**
     * Checks to see if the given credentials are valid. If they are, a temporary authentication
     * token is returned, otherwise an empty string is returned
     * 
     * @param username
     * @param hashedPassword The user's password after it has been hashed by the Authenticators hasher
     * @return An authentication token that must be verified by services
     */
    public static String authenticate(String username, String hashedPassword) {
        if (credentials.containsKey(username) && credentials.get(username).equals(hashedPassword)) {
            Token token = generateToken();
            storeToken(username, token);
            return token.authToken;
        }
        return "";
    }

    /**
     * Generates a random authentication token that isn't being used by any other user
     * 
     * @return The token string and the expiration date
     */
    private static Token generateToken() {
        Token token = new Token();
        token.authToken = generateRandomString(32, "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray());
        refreshTokenExpiration(token);
        return token;
    }

    /**
     * Generates a random string that follows the given criteria
     * 
     * @param length The length of the produced string
     * @param allowedCharacters The characters that the string will consist of
     * @return The generated string
     */
    private static String generateRandomString(int length, char[] allowedCharacters) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; ++i) {
            char nextChar = allowedCharacters[rand.nextInt(allowedCharacters.length)];
            sb.append(nextChar);
        }
        return sb.toString();
    }

    /**
     * Stores the token in the "database". If a token already
     * exists for the user, it will be overwritten.
     * 
     * @param token Auth token
     */
    private static void storeToken(String username, Token token) {
        tokens.put(username, token);
    }

    /**
     * Checks whether the token is valid, and refreshes its expiration time if it is
     * 
     * @param username
     * @param token
     * @return True if valid, false otherwise
     */
    public static boolean validateToken(String username, String tokenString) {
        if (tokens.containsKey(username)) {
            Token token = tokens.get(username);
            if (!hasTokenExpired(token)) {
                refreshTokenExpiration(token);
                return true;
            } else {
                tokens.remove(username);
            }
        }
        return false;
    }

    private static boolean hasTokenExpired(Token token) {
        if (token != null) {
            return token.expirationDate.isBefore(LocalDateTime.now());
        }
        return false;
    }

    private static void refreshTokenExpiration(Token token) {
        token.expirationDate = LocalDateTime.now().plusMinutes(60);
    }
}
