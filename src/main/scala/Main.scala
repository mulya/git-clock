import java.io.File
import java.util.concurrent.TimeUnit

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import scala.collection.JavaConversions._

/**
 *
 */
object Main extends App {

	val timePrefixSeconds = TimeUnit.MINUTES.toSeconds(30)
	val timeSplitSeconds = TimeUnit.HOURS.toSeconds(2)

	val repo = new FileRepositoryBuilder()
		.setGitDir(new File("/home/mulya/dev/projects/appraiser/.git"))
		.build()

	val git = new Git(repo)

	val start = repo.exactRef("refs/tags/m05").getObjectId
//	val finish = repo.exactRef("refs/tags/m05").getObjectId
	val finish = repo.exactRef("HEAD").getObjectId

	val commitList = git.log.addRange(start, finish).call().toList.reverse

	println(s"${commitList.size} commits")

	var totalTimeSeconds = 0L
	var comboDurationSeconds = 0L
	var isCombo = false

	var comboCount = 0L
	var singleCount = 0L

	for (i <- 0 until commitList.size - 1) {
		val currentCommitTime = commitList(i).getCommitTime
		val nextCommitTime = commitList(i + 1).getCommitTime

		if (nextCommitTime - currentCommitTime > timeSplitSeconds) {
			if (isCombo) {
				totalTimeSeconds = totalTimeSeconds + comboDurationSeconds
				comboDurationSeconds = 0L
				isCombo = false
			} else {
				totalTimeSeconds = totalTimeSeconds + timePrefixSeconds
				singleCount = singleCount + 1
			}
		} else {
			if (isCombo) {
				comboDurationSeconds = comboDurationSeconds + (nextCommitTime - currentCommitTime)
			} else {
				isCombo = true
				comboDurationSeconds = nextCommitTime - currentCommitTime
				totalTimeSeconds = totalTimeSeconds + timePrefixSeconds
				comboCount = comboCount + 1
			}
		}
	}

	val hours = TimeUnit.SECONDS.toHours(totalTimeSeconds)
	val minutes = TimeUnit.SECONDS.toMinutes(totalTimeSeconds - TimeUnit.HOURS.toSeconds(hours))

	println(s"$comboCount combos $singleCount singles")
	println(s"$hours hours $minutes minutes")
	println("")
	println(s"1 hour for 750 rubles")
	println(s"Total: ${(hours + minutes/60f) * 750}")

}
