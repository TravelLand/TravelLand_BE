package com.travelland.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.travelland.dto.member.MemberDto;
import com.travelland.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "회원 API", description = "회원 관련 API 명세서입니다.")
public interface MemberControllerDocs {

    @Operation(summary = "로그아웃", description = "로그아웃 API")
    ResponseEntity logout(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "회원탈퇴", description = "회원탈퇴 API")
    ResponseEntity signout(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "닉네임 변경", description = "닉네임 변경 API")
    ResponseEntity changeNickname(@RequestBody MemberDto.ChangeNicknameRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "닉네임 중복체크", description = "닉네임 중복체크 API")
    ResponseEntity checkNickname(@PathVariable String nickname);

    @Operation(summary = "닉네임 목록 검색", description = "닉네임 목록 검색 API")
    ResponseEntity searchNickname(@RequestParam String nickname);

    @Operation(summary = "회원정보", description = "닉네임 / 이메일 / 프로필이미지 API")
    ResponseEntity getMemberInfo();

    @Operation(summary = "마이페이지 조회", description = "마이페이지 조회 API")
    ResponseEntity getMyInfo();
}
