package yys.safewalk.application.port.in;

public record GetEmdBySidoCodeQuery(
        String sidoCode
) {
    public GetEmdBySidoCodeQuery {
        if (sidoCode == null || sidoCode.length() != 4) {
            throw new IllegalArgumentException("SidoCode must be 4 characters");
        }
    }
}