package hello.typeconverter.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @EqualsAndHashCode 를 넣으면 모든 필드를 사용해서 equals(), hashcode()를 생성함.
 * 따라서 모든 필드의 값이 같다면 `a.equals(b)` 의 결과가 참이 됨
 * (즉, equals() 메서드를 따로 구현할 필요가 없어짐)
 */
@Getter
@EqualsAndHashCode
public class IpPort {

    private String ip;
    private Integer port;

    public IpPort(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}
