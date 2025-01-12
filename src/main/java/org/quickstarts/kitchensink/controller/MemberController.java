package org.quickstarts.kitchensink.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.quickstarts.kitchensink.enums.MemberStatus;
import org.quickstarts.kitchensink.exception.IllegalOperationException;
import org.quickstarts.kitchensink.exception.MemberAlreadyExistsException;
import org.quickstarts.kitchensink.exception.MemberNotFoundException;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.pojo.ApiResponse;
import org.quickstarts.kitchensink.pojo.MemberDTO;
import org.quickstarts.kitchensink.service.MemberRegistrationService;
import org.quickstarts.kitchensink.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {
    private static final String ID_PATTERN = "^[a-fA-F0-9]{24}$";  // Regex for MongoDB ObjectId (24 hex characters)

    private final MemberService memberService;
    private final MemberRegistrationService memberRegistrationService;

    @Autowired
    public MemberController(MemberService memberService, MemberRegistrationService memberRegistrationService) {
        this.memberService = memberService;
        this.memberRegistrationService = memberRegistrationService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<ApiResponse<Member>> createMember(@RequestBody @Valid MemberDTO newMemberRequest) throws MemberNotFoundException {
        log.info("Creating member");

        boolean emailExist = memberService.isEmailExist(newMemberRequest.getEmail());
        if (emailExist) {
            throw new MemberAlreadyExistsException("Member with email " + newMemberRequest.getEmail() + " already exists.");
        }

        Member newMember = new Member(newMemberRequest.getName(), newMemberRequest.getEmail(), newMemberRequest.getPhoneNumber());
        memberRegistrationService.register(newMember);
        Member createdMember = memberService.findByEmail(newMember.getEmail()).orElseThrow(MemberNotFoundException::new);

        HttpStatus status = HttpStatus.CREATED;
        ApiResponse<Member> response = new ApiResponse<>(
                status.value(),
            "Member created successfully",
            createdMember
        );

        return new ResponseEntity<>(response, status);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ApiResponse<List<Member>>> listAllMembers() {
        log.info("Listing all members");
        List<Member> members = memberService.findAllOrderedByName().stream()
                .filter(member -> !member.isDeleted())
                .toList();

        HttpStatus status = HttpStatus.OK;
        ApiResponse<List<Member>> response = new ApiResponse<>(
            status.value(),
            null,
            members
        );

        return new ResponseEntity<>(response, status);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity<ApiResponse<Member>> lookupMemberById(
            @PathVariable
            @Pattern(regexp = ID_PATTERN, message = "Invalid Id format")
            String id
    ) throws MemberNotFoundException {
        log.info("Looking up member with id {}", id);
        Member member = memberService.findById(id).orElseThrow(MemberNotFoundException::new);
        if (member.isDeleted() || !member.isActive()) {
            throw new MemberNotFoundException();
        }

        HttpStatus status = HttpStatus.OK;
        ApiResponse<Member> response = new ApiResponse<>(
            status.value(),
            null,
            member
        );

        return new ResponseEntity<>(response, status);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity<ApiResponse<Member>> deleteMember(
            @PathVariable
            @Pattern(regexp = ID_PATTERN, message = "Invalid Id format")
            String id
    ) throws MemberNotFoundException {
        log.info("Deleting member with ID: {}", id);

        // Check if the member exists
        Member existingMember = memberService.findById(id)
                .orElseThrow(MemberNotFoundException::new);

        if (existingMember.isDeleted() || !existingMember.isActive()) {
            throw new MemberNotFoundException();
        }

        memberService.delete(existingMember);  // Delete the member

        HttpStatus status = HttpStatus.OK;
        ApiResponse<Member> response = new ApiResponse<>(
                status.value(),
                "Member successfully deleted with id: " + id,
                null
        );

        return new ResponseEntity<>(response, status);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/status/{id}")
    public ResponseEntity<ApiResponse<Member>> changeMemberStatus(
            @PathVariable
            @Pattern(regexp = ID_PATTERN, message = "Invalid Id format")
            String id,

            @RequestParam
            MemberStatus status
    ) throws MemberNotFoundException {
        log.info("Changing status for member with ID: {}", id);

        Member existingMember = memberService.findById(id)
                .orElseThrow(MemberNotFoundException::new);
        if (existingMember.isDeleted()) {
            throw new IllegalOperationException("Can not perform the action on this user.");
        }
        existingMember.setStatus(status);
        memberService.save(existingMember);

        HttpStatus httpStatus = HttpStatus.OK;
        ApiResponse<Member> response = new ApiResponse<>(
                httpStatus.value(),
                "Member with id: " + id + " successfully marked " + status,
                existingMember
        );

        return new ResponseEntity<>(response, httpStatus);
    }
}
