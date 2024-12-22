package org.quickstarts.kitchensink.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.service.MemberRegistrationService;
import org.quickstarts.kitchensink.service.MemberService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private MemberRegistrationService memberRegistrationService;

    @InjectMocks
    private MemberController memberController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    @Test
    void testCreateMember() throws Exception {
        // Arrange
        Member newMember = new Member("John Doe", "john@example.com", "9876543210");
        when(memberService.isEmailExist(newMember.getEmail())).thenReturn(false);
        when(memberService.findByEmail(newMember.getEmail())).thenReturn(Optional.of(newMember));
        doNothing().when(memberRegistrationService).register(newMember);

        // Act & Assert
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\", \"phoneNumber\":\"9876543210\"}"))
                .andExpect(status().isOk());

        // Verify interactions
        verify(memberService).isEmailExist(newMember.getEmail());
        verify(memberRegistrationService).register(any(Member.class));
    }

    @Test
    void testListAllMembers() throws Exception {
        // Arrange
        Member member1 = new Member("Alice", "alice@example.com", "9876543210");
        Member member2 = new Member("Bob", "bob@example.com", "1234567890");
        List<Member> members = List.of(member1, member2);
        when(memberService.findAllOrderedByName()).thenReturn(members);

        // Act & Assert
        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        // Verify interactions
        verify(memberService).findAllOrderedByName();
    }

    @Test
    void testLookupMemberById_found() throws Exception {
        // Arrange
        String memberId = "605c72ef1532073c73f1a7b2";  // Example MongoDB ObjectId
        Member member = new Member("John Doe", "john@example.com", "9876543210");
        member.setId(memberId);
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));

        // Act & Assert
        mockMvc.perform(get("/members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        // Verify interactions
        verify(memberService).findById(memberId);
    }

    @Test
    void testInvalidIdPattern() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/members/{id}", "invalidId"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).contains("400 BAD_REQUEST \"Validation failure\""));
    }
}
