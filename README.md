# Springmvc2-form
리포지토리 설명 : 인프런 김영한님의 강의 '스프링 MVC 2편 - 백엔드 웹 개발 활용 기술' 스터디 정리
https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-2/dashboard

# 1. 메시지, 국제화
- 메시지 :
  예를 들어 삼품명, 가격, 수량 등의 label 단어가 하드코딩되어 있을 경우 이를 변경할 때  
  여러 .html 파일에 있는 해당 label 값들을 일일이 변경해야 되는 변경해야 되는 불편함 발생  
  -> 다양한 메시지를 한 곳에서 관리하도록 하는 메시지 기능을 사용하여 해결

  <messages.properties>  
  ```
  # 메시지 관리용 파일
  item=상품  
  item.id=상품 ID  
  item.itemName=상품명  
  item.price=가격  
  item.quantity=수량
  ```

  이후 각 HTML 파일들은 다음과 같이 해당 데이터를 key 값으로 불러와서 사용
    
  <addForm.html>  
  ```<label for="itemName" th:text="#{item.itemName}"></label>```

  ※ 이러한 메시징 관리 기능을 사용하려면 스프링에서 제공하는 `MessageSource` 인터페이스를 스프링 빈으로 등록하여 사용하면 되는데,  
  스프링 부트에서는 `MessageSource` 가 자동으로 스프링 빈으로 등록되며 다음과 같이 소스를 설정하면 됨.
    
  <application.properties>   
  ```
  # 스프링 부트 메시지 소스 기본 값
  spring.messages.basename=messages
  ```
  

- 국제화 : 
  메시지에서 설명한 메시지 파일(messages.properties)을 각 나라별로 별도로 관리하면 서비스를 국제화 할 수 있음.  
    
  <messages_en.properties>
  ```
  item=Item
  item.id=Item ID
  item.itemName=Item Name
  item.price=price
  item.quantity=quantity
  ```
  템플릿 파일에 모두 #{...} 를 통해서 메시지를 사용하도록 적용했다면, messages_en.properties을 추가해주기만 하면 국제화 작업은 끝.  
  웹 브라우저의 언어 설정 값을 변경하면 국제화 적용 확인 가능.  
  (사실 스프링도 Locale 정보를 알아야 언어를 선택할 수 있는데, 스프링은 언어 선택시 기본으로 AcceptLanguage 헤더의 값을 사용.)


# 2. Validation
- BindingResult : 스프링이 제공하는 검증 오류를 보관하는 객체로, BindingResult 가 있으면  
  `@ModelAttribute` 에 데이터 바인딩 시 오류가 발생해도 컨트롤러가 호출됨!  
  (단, `BindingResult bindingResult` 파라미터의 위치는 반드시 `@ModelAttribute` 변수 바로 뒤에 있어야 함!)    

  ※ BindingResult에 검증 오류를 적용하는 3가지 방법
  1) `FieldError`, `ObjectError` 사용
  2) `rejectValue()` , `reject()` 사용
  3) `Validator` 사용
 
- `FieldError`, `ObjectError`  
- `rejectValue()`, `reject()` : FieldError, ObjectError를 직접 생성하지 않고, 깔끔하게 검증 오류 처리 가능.
  ```
  @PostMapping("/add")
  public String addItem(@ModelAttribute Item item, BindingResult bindingResult) {

    // 특정 필드(price) 예외
    if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
      bindingResult.rejectValue(
        "price",                     // field : 오류 필드명
        "range",                     // errorCode : 오류 코드(messageResolver)
        new Object[]{1000, 1000000}, // errorArgs : 오류 메시지에서 {0} 을 치환하기 위한 값
        null                         // defaultMessage : 오류 메시지를 찾을 수 없을 때 사용하는 기본 메시지
      );
    }

    // 특정 필드 예외가 아닌 전체 예외
    if (item.getPrice() != null && item.getQuantity() != null) {
      int resultPrice = item.getPrice() * item.getQuantity();
      if (resultPrice < 10000) {
        bindingResult.reject(
          "totalPriceMin",                 // errorCode
          new Object[]{10000,resultPrice}, // errorArgs
          null                             // defaultMessage
        );
      }
    }

    ...
  }
  ```
  
- Validator : `rejectValue()`, `reject()`를 활용한 검증 로직을 스프링에서 제공하는 Validator 인터페이스를 통해 별도 분리하여 관리
  ```
  @Component // 스프링 빈으로 등록하여 관리
  public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {  // 해당 검증기를 지원하는 여부 확인
      return Item.class.isAssignableFrom(clazz);
    }
  
    @Override
    public void validate(Object target, Errors errors) {  // 검증 대상 객체와 BindingResult
      Item item = (Item) target;
  
      // 필드(price) 예외
      if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
        errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
      }
  
      //특정 필드 예외가 아닌 전체 예외
      if (item.getPrice() != null && item.getQuantity() != null) {
        int resultPrice = item.getPrice() * item.getQuantity();
        if (resultPrice < 10000) {
          errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }
      }
  
      ...
    }
  }
  ```
  ```
  @PostMapping("/add")
  /* @Validated는 검증기를 실행하라는 애노테이션으로, 이 애노테이션이 붙으면 WebDataBinder에 등록한 검증기를 찾아서 실행 */
  public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult) { ... }
  ```

# 3. Bean Validation
  특정 필드에 대한 검증 로직은 주로 해당 필드가 빈 값인지 아닌지, 특정 크기를 넘는지와 같은 매우 일반적인 로직이며,  
  이러한 검증 로직을 모든 프로젝트에 적용할 수 있게 공통화하고 표준화 한 것으로,  
  다음과 같은 검증 애노테이션 지원  
    
  `@NotBlank` : 빈값 + 공백만 있는 경우를 허용하지 않음  
  `@NotNull` : `null` 을 허용하지 않음  
  `@Range(min = 1000, max = 1000000)` : 범위 안의 값이어야 함    
  `@Max(9999)` : 최대 9999까지만 허용  

  ex)
  ```
  @Data
  public class Item {

    @NotNull
    private Long id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;

    @NotNull
    @Max(9999)
    private Integer quantity;
    ...
  }
  ```

  - 스프링과 통합하지 않은 순수 Bean Validation은 다음과 같이 검증기를 직접 생성해서 사용해야 함  
  ```
  ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  Validator validator = factory.getValidator();
  Set<ConstraintViolation<Item>> violations = validator.validate(item); // 검증 대상(item)을 직접 검증기에 넣고 그 결과를 리턴
  ```

  - 스프링 부트와 통합
  스프링 부트는 자동으로 LocalValidatorFactoryBean 을 글로벌 Validator로 등록하며,  
  이 Validator는 `@NotNull` 같은 애노테이션을 보고 검증을 수행.  
  따라서 스프링 부트에서는 이렇게 글로벌 Validator가 자동으로 적용되어 있기 때문에, `@Valid` , `@Validated` 만 적용하면 됨.  
  ※ 한편, BeanValidator는 바인딩에 실패한 필드는 BeanValidation을 적용하지 않음.  
    
  - BeanValidator에서 에러 메시지 찾는 순서  
    1. 생성된 메시지 코드 순서대로 `messageSource` 에서 메시지 찾기  
    2. 애노테이션의 `message` 속성 사용 -> `@NotBlank(message = "공백! {0}")`  
    3. 라이브러리가 제공하는 기본 값 사용 -> "공백일 수 없습니다" 

  - Bean Validation 한계  
  등록시에는 `id` 에 값이 없어도 되지만, 수정시에는 `id` 값이 필수여야 하는 등 데이터를 등록할 때와 수정할 때 필드에 대한 검증 요구사항이 다를 수 있음.  
  -> 폼을 등록용과 수정용 객체로 구분하여 관리
    
    ex)  
    - 등록용 폼 : ItemSaveForm 객체  
    - 수정용 폼 : ItemUpdateForm 객체  
    
    
  - Bean Validation HTTP 메시지 컨버터 적용  
  `@Valid` , `@Validated` 는 `HttpMessageConverter`(`@RequestBody`)에도 적용 가능

    ex)  
    ```
    @Slf4j
    @RestController
    @RequestMapping("/validation/api/items")
    public class ValidationItemApiController {
  
      @PostMapping("/add")
      public Object addItem(@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult) {
        log.info("API 컨트롤러 호출");
        ...
      }
    ```
      
    참고)  
    `@ModelAttribute` 는 HTTP 요청 파라미터(URL 쿼리 스트링, POST Form)를 다룰 때 사용.  
    `@RequestBody` 는 HTTP Body의 데이터를 객체로 변환할 때 사용하며, 주로 API JSON 요청을 다룰 때 사용.  
      
    ※ `@ModelAttribute` vs `@RequestBody`  
    - `@ModelAttribute` 는 필드 단위로 정교하게 바인딩이 적용되며, 특정 필드가 바인딩 되지 않아도  
      나머지 필드는 정상 바인딩 되고 Validator를 사용한 검증도 적용할 수 있음.  
    - 반면, `@RequestBody` 는 HttpMessageConverter 단계에서 JSON 데이터를 객체로 변경하지 못하면  
      이후 단계 자체가 진행되지 않고 예외가 발생. 컨트롤러도 호출되지 않고, Validator도 적용할 수 없음.

# 4. 쿠키, 세션
  - 쿠키 : 사용자의 로그인 상태를 유지하기 위한 방법으로, 서버에서 로그인에 성공하면 HTTP 응답에 쿠키값을 담아서 브라우저에 전달.  
          그럼 해당 브라우저는 앞으로 요청을 할 때마다 해당 쿠키 값을 지속해서 보내줌.  

    ![image](https://github.com/hyeda2020/Springmvc2-form/assets/139141270/2addbbca-ce38-4e4e-9970-79502d433082)

    ※ 영속쿠키, 세션쿠키  
    1) 영속쿠키 : 만료 날짜를 입력하면 해당 날짜까지 유지  
    2) 세션쿠키 : 만료 날짜를 생략하면 브라우저 종료시 까지만 유지  
    -> 보통 브라우저 종료시 로그아웃 되길 기대하므로 주로 필요한 것은 세션 쿠키.

  - 쿠키와 보안 문제  
    1) 쿠키 값은 임의로 변경할 수 있으며, 클라이언트가 쿠키를 강제로 변경하면 서버에서는 다른 사용자로 인식할 수 있음.  
    2) 쿠키에 보관된 정보는 훔쳐갈 수 있음.  

  - 세션 : 쿠키에 중요한 값을 세팅하지 않고 사용자별로 임의의 토큰(랜덤 값)을 전달하며,  
          서버에서는 토큰과 사용자 ID를 매핑하여 인식. 또한, 이러한 토큰은 서버에서 관리.  
          또한, 토큰이 해킹되어도 시간이 지나면 사용할 수 없도록 토큰의 만료시간을 짧게 유지(30분)   

    ![image](https://github.com/hyeda2020/Springmvc2-form/assets/139141270/515476ca-97eb-4182-9195-b50b610e3acb)

  - HttpSession
    서블릿은 세션을 위해 `HttpSession` 이라는 기능을 제공.  
    
    ```
    @RequestMapping("/login")
    public String login(@Validated @ModelAttribute LoginForm form, HttpServletRequest request) {
      ...
      // 세션이 있으면 있는 세션 반환, 없으면 신규 세션 생성 (true)
      HttpSession session = request.getSession(true);

      // 세션에 로그인 회원 정보 보관
      session.setAttribute(SessionConst.LOGIN_MEMBER.getValue(), loginMember)
      ...
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
      
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate(); // 세션 삭제
      }
      return "redirect:/";
    }
    ```
    
- @SessionAttribute : 이미 로그인 된 사용자를 찾을 때 스프링에서는 세션을 더 편리하게 사용할 수 있도록 `@SessionAttribute` 제공.
    
  ```
  @GetMapping("/")
  public String LoginHome(@SessionAttribute(name = "loginMember", required = false) Member member, Model model) {
    ...
  }
  ```
