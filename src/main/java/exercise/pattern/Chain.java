package exercise.pattern;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Chain {
    static void main() {
        AuthHandler chain = new RateLimitHandler();
        chain.setNext(new AuthenticationHandler()).setNext(new AuthorizationHandler());

        Request request = new Request("192.168.1.1", "Bearer token123", "/api/users");
        boolean allowed = chain.handle(request);
    }
}

// Handler
abstract class AuthHandler {
    protected AuthHandler next;

    public AuthHandler setNext(AuthHandler handler) {
        this.next = handler;
        return handler;  // для fluent API
    }

    public abstract boolean handle(Request request);

    protected boolean handleNext(Request request) {
        if (next == null) return true;  // конец цепочки
        return next.handle(request);
    }
}

// Конкретные обработчики
class RateLimitHandler extends AuthHandler {
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    public boolean handle(Request request) {
        int count = requestCounts.merge(request.ip(), 1, Integer::sum);
        if (count > 100) {
            System.out.println("Rate limit exceeded for " + request.ip());
            return false;
        }
        return handleNext(request);
    }
}

class AuthenticationHandler extends AuthHandler {
    public boolean handle(Request request) {
        if (request.token() == null || !isValidToken(request.token())) {
            System.out.println("Authentication failed");
            return false;
        }
        return handleNext(request);
    }

    private boolean isValidToken(String token) {
        return token.startsWith("Bearer ");
    }
}

class AuthorizationHandler extends AuthHandler {
    public boolean handle(Request request) {
        if (!hasPermission(request.token(), request.resource())) {
            System.out.println("Access denied to " + request.resource());
            return false;
        }
        return handleNext(request);
    }

    private boolean hasPermission(String token, String resource) {
        return true;  // simplified
    }
}

record Request(String ip, String token, String resource) {}

