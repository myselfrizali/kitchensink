package org.quickstarts.kitchensink.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.quickstarts.kitchensink.enums.MemberStatus;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.pojo.ApiError;
import org.quickstarts.kitchensink.pojo.ApiResponse;
import org.quickstarts.kitchensink.pojo.FieldError;
import org.quickstarts.kitchensink.service.MemberRegistrationService;
import org.quickstarts.kitchensink.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {
    private static ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private MemberRegistrationService memberRegistrationService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void testCreateMember_successfullyCreateMember() throws Exception {
        // Arrange
        Member newMember = new Member("John Doe", "john@example.com", "9876543210");
        when(memberService.isEmailExist(newMember.getEmail())).thenReturn(false);
        when(memberService.findByEmail(newMember.getEmail())).thenReturn(Optional.of(newMember));
        doNothing().when(memberRegistrationService).register(newMember);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\", \"phoneNumber\":\"9876543210\"}")
                ).andExpect(status().isCreated())
                .andReturn();

        // Verify interactions
        verify(memberService).isEmailExist(newMember.getEmail());
        verify(memberRegistrationService).register(any(Member.class));
        ApiResponse<Member> apiResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>(){});
        assertThat(apiResponse.getStatus()).isEqualTo(201);
        assertThat(apiResponse.getMessage()).isEqualTo("Member created successfully");
        assertThat(apiResponse.getData()).isNotNull();
    }

    @Test
    @WithMockUser
    void testCreateMember_memberAlreadyExists() throws Exception {
        String requestBody = "{\"name\":\"John Doe\",\"email\":\"john@example.com\", \"phoneNumber\":\"9876543210\"}";
        String email = "john@example.com";
        when(memberService.isEmailExist(eq(email))).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService).isEmailExist(email);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Member with email john@example.com already exists.");
        assertThat(apiError.getDetails()).isNull();
    }

    @WithMockUser
    @ParameterizedTest
    @CsvSource({"' ', Name is mandatory", "1213, Must not contain numbers", "john32432, Must not contain numbers"})
    void testCreateMember_invalidMemberName(String name, String expected) throws Exception {
        String requestBody = "{\"name\":\"" + name + "\",\"email\":\"john@example.com\", \"phoneNumber\":\"9876543210\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService, never()).isEmailExist(any());
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);

        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo(expected);
    }

    @WithMockUser
    @ParameterizedTest
    @ValueSource(strings = {""})
    void testCreateMember_memberNameIsNull(String name) throws Exception {
        String requestBody = "{\"name\":\"" + name + "\",\"email\":\"john@example.com\", \"phoneNumber\":\"9876543210\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService, never()).isEmailExist(any());
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(2);
        assertThat(apiError.getDetails().get(0).getField()).isEqualTo("name");
        assertThat(apiError.getDetails().get(1).getField()).isEqualTo("name");
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).containsExactlyInAnyOrder("Name is mandatory", "Name must of length 1 to 25");
    }

    @WithMockUser
    @ParameterizedTest
    @CsvSource({"john32432, Invalid email", "john32432@cac, Invalid email", "john32432@cac., Invalid email"})
    void testCreateMember_invalidEmail(String email, String expected) throws Exception {
        String requestBody = "{\"name\":\"John Doe\",\"email\":\"" + email + "\", \"phoneNumber\":\"9876543210\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService, never()).isEmailExist(any());
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo(expected);
    }

    @WithMockUser
    @ParameterizedTest
    @ValueSource(strings = {" "})
    void testCreateMember_emailIsEmptyOrNull(String email) throws Exception {
        String requestBody = "{\"name\":\"John Doe\",\"email\":\"" + email + "\", \"phoneNumber\":\"9876543210\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService, never()).isEmailExist(any());
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(2);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).containsExactlyInAnyOrder("Email is mandatory", "Invalid email");
    }

    @Test
    @WithMockUser
    void getAllMembers_shouldSuccessfullyReturnNonDeletedMembers() throws Exception {
        // Arrange
        Member mockMember = mock(Member.class);
        Member mockMember1 = mock(Member.class);
        when(memberService.findAllOrderedByName()).thenReturn(List.of(mockMember, mockMember1));
        when(mockMember.isDeleted()).thenReturn(false);
        when(mockMember1.isDeleted()).thenReturn(true);


        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isOk())
                .andReturn();

        verify(memberService).findAllOrderedByName();
        ApiResponse<List<Member>> apiResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(apiResponse.getStatus()).isEqualTo(200);
        assertThat(apiResponse.getData()).isNotNull();
        assertThat(apiResponse.getData()).hasSize(1);
    }

    @Test
    @WithMockUser
    void getMemberById_invalidIdFormat() throws Exception {
        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/{id}", "invalidId"))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService, never()).findById(any());
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Invalid Id format");
    }

    @Test
    @WithMockUser
    void getMemberById_memberNotExist() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        when(memberService.findById(eq(memberId))).thenReturn(Optional.empty());

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/{id}", memberId))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("Member not found or deleted");
        assertThat(apiError.getDetails()).isNull();
    }

    @Test
    @WithMockUser
    void getMemberById_memberIsDeleted() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        Member mockMember = mock(Member.class);
        when(memberService.findById(eq(memberId))).thenReturn(Optional.of(mockMember));
        when(mockMember.isDeleted()).thenReturn(true);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/{id}", memberId))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("Member not found or deleted");
        assertThat(apiError.getDetails()).isNull();
    }

    @Test
    @WithMockUser
    void getMemberById_memberIsNotActive() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        Member mockMember = mock(Member.class);
        when(memberService.findById(eq(memberId))).thenReturn(Optional.of(mockMember));
        when(mockMember.isDeleted()).thenReturn(false);
        when(mockMember.isActive()).thenReturn(false);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/{id}", memberId))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("Member not found or deleted");
        assertThat(apiError.getDetails()).isNull();
    }

    @Test
    @WithMockUser
    void getMemberById_successfullyExecutes() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        Member mockMember = mock(Member.class);
        when(memberService.findById(eq(memberId))).thenReturn(Optional.of(mockMember));
        when(mockMember.isDeleted()).thenReturn(false);
        when(mockMember.isActive()).thenReturn(true);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/{id}", memberId))
                .andExpect(status().isOk())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiResponse<Member> apiResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(apiResponse.getStatus()).isEqualTo(200);
        assertThat(apiResponse.getData()).isNotNull();
    }

    @Test
    @WithMockUser
    void deleteMember_invalidIdFormat() throws Exception {
        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/members/{id}", "invalidId"))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService, never()).findById(any());
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Invalid Id format");
    }

    @Test
    @WithMockUser
    void deleteMember_memberNotExist() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        when(memberService.findById(eq(memberId))).thenReturn(Optional.empty());

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/members/{id}", memberId))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("Member not found or deleted");
        assertThat(apiError.getDetails()).isNull();
    }

    @Test
    @WithMockUser
    void deleteMember_memberIsDeleted() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        Member mockMember = mock(Member.class);
        when(memberService.findById(eq(memberId))).thenReturn(Optional.of(mockMember));
        when(mockMember.isDeleted()).thenReturn(true);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/members/{id}", memberId))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("Member not found or deleted");
        assertThat(apiError.getDetails()).isNull();
    }

    @Test
    @WithMockUser
    void deleteMember_memberIsNotActive() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        Member mockMember = mock(Member.class);
        when(memberService.findById(eq(memberId))).thenReturn(Optional.of(mockMember));
        when(mockMember.isDeleted()).thenReturn(false);
        when(mockMember.isActive()).thenReturn(false);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/members/{id}", memberId))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("Member not found or deleted");
        assertThat(apiError.getDetails()).isNull();
    }

    @Test
    @WithMockUser
    void deleteMember_successfullyExecutes() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        Member mockMember = mock(Member.class);
        when(memberService.findById(eq(memberId))).thenReturn(Optional.of(mockMember));
        when(mockMember.isDeleted()).thenReturn(false);
        when(mockMember.isActive()).thenReturn(true);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/members/{id}", memberId))
                .andExpect(status().isOk())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiResponse<Member> apiResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(apiResponse.getStatus()).isEqualTo(200);
        assertThat(apiResponse.getMessage()).isEqualTo("Member successfully deleted with id: 6778007913f34819876ffff5");
        assertThat(apiResponse.getData()).isNull();
    }

    @Test
    @WithMockUser
    void changeMemberStatus_invalidIdFormat() throws Exception {
        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/members/status/{id}?status=ACTIVE", "invalidId"))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService, never()).findById(any());
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Invalid Id format");
    }

    @Test
    @WithMockUser
    void changeMemberStatus_memberNotExist() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        when(memberService.findById(eq(memberId))).thenReturn(Optional.empty());

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/members/status/{id}?status=ACTIVE", memberId))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("Member not found or deleted");
        assertThat(apiError.getDetails()).isNull();
    }

    @Test
    @WithMockUser
    void changeMemberStatus_shouldRaiseExceptionForDeletedMember() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        Member mockMember = mock(Member.class);
        when(memberService.findById(eq(memberId))).thenReturn(Optional.of(mockMember));
        when(mockMember.isDeleted()).thenReturn(true);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/members/status/{id}?status=ACTIVE", memberId))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(memberService).findById(memberId);
        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Can not perform the action on this user.");
        assertThat(apiError.getDetails()).isNull();
    }

    @Test
    @WithMockUser
    void changeMemberStatus_successfullyExecutes() throws Exception {
        // Arrange
        String memberId = "6778007913f34819876ffff5";
        Member mockMember = mock(Member.class);
        when(memberService.findById(eq(memberId))).thenReturn(Optional.of(mockMember));
        when(mockMember.isDeleted()).thenReturn(false);
        doNothing().when(mockMember).setStatus(any(MemberStatus.class));
        doNothing().when(memberService).save(mockMember);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/members/status/{id}?status=ACTIVE", memberId))
                .andExpect(status().isOk())
                .andReturn();

        verify(memberService).findById(memberId);
        verify(mockMember).setStatus(any(MemberStatus.class));
        ApiResponse<Member> apiResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(apiResponse.getStatus()).isEqualTo(200);
        assertThat(apiResponse.getMessage()).isEqualTo("Member with id: 6778007913f34819876ffff5 successfully marked ACTIVE");
        assertThat(apiResponse.getData()).isNotNull();
    }
}