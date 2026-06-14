package ro.puk3p.sentinel.rule.controller

import jakarta.validation.Valid
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ro.puk3p.sentinel.common.response.ApiResponse
import ro.puk3p.sentinel.rule.assembler.RuleModelAssembler
import ro.puk3p.sentinel.rule.dto.RuleResponse
import ro.puk3p.sentinel.rule.dto.RuleUpsertRequest
import ro.puk3p.sentinel.rule.model.RuleCategory
import ro.puk3p.sentinel.rule.service.RuleService

@RestController
@RequestMapping("/api/rules")
class RuleController(
    private val ruleService: RuleService,
    private val assembler: RuleModelAssembler,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) category: RuleCategory?,
        @RequestParam(required = false) interfaceScope: String?,
        @RequestParam(required = false) deviceId: String?,
    ): ApiResponse<CollectionModel<EntityModel<RuleResponse>>> {
        val items = ruleService.list(category, interfaceScope, deviceId).map(assembler::toModel)
        val model =
            CollectionModel.of(
                items,
                linkTo(methodOn(RuleController::class.java).list(category, interfaceScope, deviceId)).withSelfRel(),
            )
        return ApiResponse(success = true, message = "Rules retrieved", data = model)
    }

    @GetMapping("/{ruleId}")
    fun getById(
        @PathVariable ruleId: String,
    ): ApiResponse<EntityModel<RuleResponse>> =
        ApiResponse(success = true, message = "Rule retrieved", data = assembler.toModel(ruleService.get(ruleId)))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: RuleUpsertRequest,
    ): ApiResponse<EntityModel<RuleResponse>> =
        ApiResponse(success = true, message = "Rule created", data = assembler.toModel(ruleService.create(request)))

    @PutMapping("/{ruleId}")
    fun update(
        @PathVariable ruleId: String,
        @Valid @RequestBody request: RuleUpsertRequest?,
    ): ApiResponse<EntityModel<RuleResponse>> =
        ApiResponse(
            success = true,
            message = "Rule updated",
            data = assembler.toModel(ruleService.update(ruleId, request ?: RuleUpsertRequest())),
        )

    @PostMapping("/{ruleId}/enable")
    fun enable(
        @PathVariable ruleId: String,
    ): ApiResponse<EntityModel<RuleResponse>> =
        ApiResponse(success = true, message = "Rule enabled", data = assembler.toModel(ruleService.setEnabled(ruleId, true)))

    @PostMapping("/{ruleId}/disable")
    fun disable(
        @PathVariable ruleId: String,
    ): ApiResponse<EntityModel<RuleResponse>> =
        ApiResponse(success = true, message = "Rule disabled", data = assembler.toModel(ruleService.setEnabled(ruleId, false)))

    @PostMapping("/{ruleId}/deploy")
    fun deploy(
        @PathVariable ruleId: String,
    ): ApiResponse<EntityModel<RuleResponse>> =
        ApiResponse(success = true, message = "Rule deployed", data = assembler.toModel(ruleService.deploy(ruleId)))

    @DeleteMapping("/{ruleId}")
    fun delete(
        @PathVariable ruleId: String,
    ): ApiResponse<Unit> {
        ruleService.delete(ruleId)
        return ApiResponse(success = true, message = "Rule deleted", data = Unit)
    }
}
