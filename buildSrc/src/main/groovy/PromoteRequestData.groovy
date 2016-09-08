class PromoteRequestData {
	def data

	PromoteRequestData(final String stagedRepositoryId, final String stagingProfileId, final String description) {
		final PromoteRequest promoteRequest = new PromoteRequest()
		promoteRequest.setStagedRepositoryId(stagedRepositoryId)
		promoteRequest.setTargetRepositoryId(stagingProfileId)
		promoteRequest.setDescription(description)

		data = promoteRequest
	}
}
