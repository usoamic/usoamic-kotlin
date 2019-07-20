package io.usoamic.cli.core

import io.usoamic.cli.enum.IdeaStatus
import io.usoamic.cli.enum.VoteType
import io.usoamic.cli.model.Idea
import io.usoamic.cli.model.Vote
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.lang.Exception
import java.math.BigInteger

open class Ideas constructor(filename: String, contractAddress: String, node: String) :
    Owner(filename, contractAddress, node) {

    @Throws(Exception::class)
    fun addIdea(password: String, description: String): String = executeTransaction(
        password,
        "addIdea",
        listOf(Utf8String(description))
    )

    @Throws(Exception::class)
    fun setIdeaStatus(password: String, ideaRefId: BigInteger, status: IdeaStatus) = executeTransaction(
        password,
        "setIdeaStatus",
        listOf(
            Uint256(ideaRefId),
            Uint8(status.ordinal.toLong())
        )
    )

    @Throws(Exception::class)
    fun supportIdea(password: String, ideaRefId: BigInteger, comment: String): String =
        voteForIdea(password, VoteType.SUPPORT, ideaRefId, comment)

    @Throws(Exception::class)
    fun abstainIdea(password: String, ideaRefId: BigInteger, comment: String): String =
        voteForIdea(password, VoteType.ABSTAIN, ideaRefId, comment)

    @Throws(Exception::class)
    fun againstIdea(password: String, ideaRefId: BigInteger, comment: String): String =
        voteForIdea(password, VoteType.AGAINST, ideaRefId, comment)

    @Throws(Exception::class)
    private fun voteForIdea(password: String, voteType: VoteType, ideaRefId: BigInteger, comment: String): String =
        executeTransaction(
            password,
            when (voteType) {
                VoteType.SUPPORT -> "supportIdea"
                VoteType.ABSTAIN -> "abstainIdea"
                VoteType.AGAINST -> "againstIdea"
            },
            listOf(
                Uint256(ideaRefId),
                Utf8String(comment)
            )
        )

    @Throws(Exception::class)
    fun getIdea(ideaRefId: BigInteger): Idea = getAndPrepareIdea(
        "getIdea",
        listOf(Uint256(ideaRefId))
    )

    @Throws(Exception::class)
    fun getIdeaByAddress(author: String, ideaId: BigInteger): Idea = getAndPrepareIdea(
        "getIdeaByAddress",
        listOf(
            Address(author),
            Uint256(ideaId)
        )
    )

    @Throws(Exception::class)
    fun getVote(ideaRefId: BigInteger, voteRefId: BigInteger): Vote = getAndPrepareVote(
        "getVote",
        listOf(
            Uint256(ideaRefId),
            Uint256(voteRefId)
        )
    )

    @Throws(Exception::class)
    fun getVoteByAddress(ideaRefId: BigInteger, voter: String, voteId: BigInteger): Vote = getAndPrepareVote(
        "",
        listOf(
            Uint256(ideaRefId),
            Address(voter),
            Uint256(voteId)
        )
    )


    @Throws(Exception::class)
    fun getNumberOfIdeas(): BigInteger? = executeCallEmptyPassValueAndUint256Return("getNumberOfIdeas")

    @Throws(Exception::class)
    fun getNumberOfIdeasByAddress(author: String): BigInteger? = executeCallUint256ValueReturn(
        "getNumberOfIdeas", //TODO: Rename to getNumberOfIdeasAddress
        listOf(Address(author))
    )

    @Throws(Exception::class)
    fun getNumberOfVotesByAddress(voter: String, ideaRefId: BigInteger): BigInteger? = executeCallUint256ValueReturn(
        "getNumberOfVotesByAddress",
        listOf(
            Address(voter),
            Uint256(ideaRefId)
        )
    )

    @Throws(Exception::class)
    fun getLastIdeaId(): BigInteger = getNumberOfIdeas()!!.subtract(BigInteger.ONE)

    @Throws(Exception::class)
    fun getLastIdeaIdByAddress(author: String): BigInteger = getNumberOfIdeasByAddress(author)!!.subtract(BigInteger.ONE)

    private fun getAndPrepareIdea(name: String, inputParameters: List<Type<out Any>>): Idea {
        val function = Function(
            name,
            inputParameters,
            getIdeaOutputParameters()
        )
        val result = executeCall(function)
        val ideaStatusId = result[5].value as BigInteger

        return Idea.Builder()
            .setIsExist(result[0].value as Boolean)
            .setIdeaId(result[1].value as BigInteger)
            .setIdeaRefId(result[2].value as BigInteger)
            .setAuthor(result[3].value as String)
            .setDescription(result[4].value as String)
            .setIdeaStatus(IdeaStatus.values()[ideaStatusId.toInt()])
            .setTimestamp(result[6].value as BigInteger)
            .setNumberOfSupporters(result[7].value as BigInteger)
            .setNumberOfAbstained(result[8].value as BigInteger)
            .setNumberOfVotedAgainst(result[9].value as BigInteger)
            .setNumberOfParticipants(result[10].value as BigInteger)
            .build()
    }

    private fun getAndPrepareVote(name: String, inputParameters: List<Type<out Any>>): Vote {
        val function = Function(
            name,
            inputParameters,
            getVoteOutputParameters()
        )
        val result = executeCall(function)

        val voteTypeId = result[4].value as BigInteger

        return Vote.Builder()
            .setIsExist(result[0].value as Boolean)
            .setIdeaId(result[1].value as BigInteger)
            .setVoteId(result[2].value as BigInteger)
            .setVoter(result[3].value as String)
            .setVoteType(VoteType.values()[voteTypeId.toInt()])
            .setComment(result[5].value as String)
            .build()
    }

    private fun getIdeaOutputParameters() = listOf(
        object : TypeReference<Bool>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Address>() {},
        object : TypeReference<Utf8String>() {},
        object : TypeReference<Uint8>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Uint256>() {}
    )

    private fun getVoteOutputParameters() = listOf(
        object : TypeReference<Bool>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Address>() {},
        object : TypeReference<Uint256>() {},
        object : TypeReference<Utf8String>() {}
    )
}