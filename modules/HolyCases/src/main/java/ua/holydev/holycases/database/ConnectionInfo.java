package ua.holydev.holycases.database;

public record ConnectionInfo(String host, int port, String database, String user, String password, int groupConcatLength) {
}
