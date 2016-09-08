import org.apache.commons.lang3.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.restlet.Context
import org.restlet.data.ChallengeScheme
import org.restlet.data.MediaType
import org.restlet.data.Method
import org.restlet.ext.json.JsonRepresentation
import org.restlet.representation.Representation
import org.restlet.resource.ClientResource

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser

class StagingRepositoryTask extends DefaultTask {
	private final Context context = new Context()
	private final Gson gson = new Gson()
	private final JsonParser jsonParser = new JsonParser()

	def sonatypeStagingProfileName

	@TaskAction
	public void performRelease() {
		try {
			final String stagingProfileId = getStagingProfileId()
			if (StringUtils.isBlank(stagingProfileId)) {
				println "Could not find a staging profile id so closing will not be attempted"
				return
			}

			String repositoryId = getRepositoryId("open", stagingProfileId)
			if (StringUtils.isBlank(repositoryId)) {
				println "Could not find an open repository id so closing will not be attempted"
				return
			}

			PromoteRequestData promoteRequestData = new PromoteRequestData(repositoryId, stagingProfileId,
					"test closing description")
			closeStagingRepository(stagingProfileId, promoteRequestData)

			final int retryCount = 0
			repositoryId = getRepositoryId("closed", stagingProfileId)
			while (StringUtils.isBlank(repositoryId) && retryCount < 50) {
				Thread.sleep(2000)
				repositoryId = getRepositoryId("closed", stagingProfileId)
			}

			if (StringUtils.isBlank(repositoryId)) {
				println "Could not find a closed repository id so releasing will not be attempted"
				return
			}

			promoteRequestData = new PromoteRequestData(repositoryId, stagingProfileId,
					"test promoting description")
			promoteStagingRepository(stagingProfileId, promoteRequestData)
		} catch (final Exception e) {
			println "Couldn't update the staging repository: " + e.getMessage()
		}
	}

	private void closeStagingRepository(final String stagingProfileId, final PromoteRequestData promoteRequestData)
	throws URISyntaxException {
		performAction(stagingProfileId, promoteRequestData, "finish")
	}

	private void promoteStagingRepository(final String stagingProfileId, final PromoteRequestData promoteRequestData)
	throws URISyntaxException {
		performAction(stagingProfileId, promoteRequestData, "promote")
	}

	private void performAction(final String stagingProfileId, final PromoteRequestData promoteRequestData,
			final String action) throws URISyntaxException {
		final String urlSuffix = "/staging/profiles/" + stagingProfileId + "/" + action
		final ClientResource resource = buildClientResource(urlSuffix)
		resource.getRequest().setEntity(new JsonRepresentation(gson.toJson(promoteRequestData)))
		resource.setMethod(Method.POST)
		resource.handle()
	}

	private String getRepositoryId(final String type, final String stagingProfileId) {
		String repositoryId = ""
		try {
			final String getProfileRepositories = "/staging/profile_repositories/" + stagingProfileId
			final ClientResource resource = buildClientResource(getProfileRepositories)
			final Representation response = resource.get(MediaType.APPLICATION_JSON)
			final JsonElement json = jsonParser.parse(response.getText())
			final JsonArray dataArray = json.getAsJsonObject().get("data").getAsJsonArray()
			for (final JsonElement stagingProfileRepository : dataArray) {
				final String stagingProfileRepositoryStatus = stagingProfileRepository.getAsJsonObject().get("type")
						.getAsString()
				if (type.equals(stagingProfileRepositoryStatus)) {
					repositoryId = stagingProfileRepository.getAsJsonObject().get("repositoryId").getAsString()
				}
			}
		} catch (final Exception e) {
			println "Couldn't get the repository id: " + e.getMessage()
		}

		return repositoryId
	}

	private String getStagingProfileId() {
		String stagingProfileId = ""
		try {
			final String getAllStagingProfiles = "/staging/profiles"
			final ClientResource resource = buildClientResource(getAllStagingProfiles)
			final Representation response = resource.get(MediaType.APPLICATION_JSON)
			final JsonElement json = jsonParser.parse(response.getText())
			final JsonArray dataArray = json.getAsJsonObject().get("data").getAsJsonArray()
			for (final JsonElement stagingProfile : dataArray) {
				final String stagingProfileName = stagingProfile.getAsJsonObject().get("name").getAsString()
				if (sonatypeStagingProfileName.equals(stagingProfileName)) {
					stagingProfileId = stagingProfile.getAsJsonObject().get("id").getAsString()
				}
			}
		} catch (final Exception e) {
			println "Couldn't get the staging profile id: " + e.getMessage()
		}
		return stagingProfileId
	}

	private ClientResource buildClientResource(final String urlSuffix) throws URISyntaxException {
		final URI uri = new URI("https://oss.sonatype.org/service/local" + urlSuffix)
		final ClientResource resource = new ClientResource(context, uri)
		resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, project.property('sonatypeUsername'), project.property('sonatypePassword'))
		return resource
	}

}
