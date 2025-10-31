package org.ecom.model.auth;

import java.util.Objects;

public class DeleteRequest {
    private String password;  // For confirmation in self-delete

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DeleteRequest() {
    }

    public DeleteRequest(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeleteRequest that = (DeleteRequest) o;
        return Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(password);
    }

    @Override
    public String toString() {
        return "DeleteRequest{" +
                "password='" + password + '\'' +
                '}';
    }
}
