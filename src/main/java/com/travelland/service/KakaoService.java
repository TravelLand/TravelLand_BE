package com.travelland.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelland.constant.Gender;
import com.travelland.constant.Role;
import com.travelland.domain.member.Member;
import com.travelland.domain.member.RefreshToken;
import com.travelland.dto.MemberDto;
import com.travelland.global.jwt.JwtUtil;
import com.travelland.repository.member.MemberRepository;
import com.travelland.repository.member.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public boolean kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = getToken(code);

        MemberDto.KakaoInfo kakaoUserInfo = getKakaoUserInfo(accessToken);

        Member member = registerKakaoUserIfNeeded(kakaoUserInfo);

        String createToken = jwtUtil.createToken(member.getEmail(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken();

        refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken, createToken));
        jwtUtil.addJwtToCookie(createToken, response);

        return true;
    }

    private String getToken(String code) throws JsonProcessingException {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "aa4b6a242e99488886c885baee1cd1ab");
        body.add("redirect_uri", "http://localhost:8080/users/login/kakao");
        body.add("code", code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

    private MemberDto.KakaoInfo getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();
        String name = jsonNode.get("kakao_account")
                .get("name").asText();
        String birth = jsonNode.get("kakao_account")
                .get("birthyear").asText();
        birth += jsonNode.get("kakao_account")
                .get("birthday").asText();
        String gender = jsonNode.get("kakao_account")
                .get("gender").asText();
        String profileImage = jsonNode.get("kakao_account")
                .get("profile")
                .get("profile_image_url").asText();
        String thumbnailProfileImage = jsonNode.get("kakao_account")
                .get("profile")
                .get("thumbnail_image_url").asText();

        log.info("카카오 사용자 정보: id=" + id + ", nickname=" + nickname + ", email=" + email + ", name=" + name + ", birthday=" + birth + ", gender=" + gender);
        return MemberDto.KakaoInfo.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .name(name)
                .birth(birth)
                .gender(gender)
                .profileImage(profileImage)
                .thumbnailProfileImage(thumbnailProfileImage)
                .build();
    }

    @Transactional
    public Member registerKakaoUserIfNeeded(MemberDto.KakaoInfo kakaoUserInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoUserInfo.getId();
        Member kakaoUser = memberRepository.findBySocialId(kakaoId).orElse(null);

        if (kakaoUser == null) {
            // 신규 회원가입
            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            // email: kakao email
            String email = kakaoUserInfo.getEmail();

            // birth: String -> LocalDate
            LocalDate birth = LocalDate.of(Integer.parseInt(kakaoUserInfo.getBirth().substring(0,4)),
                    Integer.parseInt(kakaoUserInfo.getBirth().substring(4,6)),
                    Integer.parseInt(kakaoUserInfo.getBirth().substring(6,8)));

            // gender
            Gender gender = Gender.MALE;
            if (kakaoUserInfo.getGender().equals("female"))
                gender = Gender.FEMALE;

            kakaoUser = Member.builder()
                    .socialId(kakaoId)
                    .password(encodedPassword)
                    .email(email)
                    .birth(birth)
                    .gender(gender)
                    .nickname(kakaoUserInfo.getNickname())
                    .role(Role.USER)
                    .name(kakaoUserInfo.getName())
                    .build();

            memberRepository.save(kakaoUser);
        }

        if (kakaoUser.getProfileImage() == null || !kakaoUser.getProfileImage().equals(kakaoUserInfo.getProfileImage()))
            kakaoUser.changeProfileImage(kakaoUserInfo.getProfileImage(), kakaoUserInfo.getThumbnailProfileImage());

        return kakaoUser;
    }
}
