package dima.dsl


/**
 * Created with IntelliJ IDEA.
 * User: SD238805
 * Date: 30/07/13
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

  abstract class Planner {
    type Action // The agent possible actions

    type UserPreference //allows to compare contexts for a specific agent

    type Context //holds the variables required to compute the result of an action

    // a politic associate a reaction to each possible situation // call of the expert system
    def politic(c: Context, p: UserPreference): Action

    //for a given politics, returns the set of action that can be computed
    def possiblePolitic(c: Context, p: UserPreference): Seq[Action]

    //Predicate the result of an action// call of the expert system
    def environment(c: Context, a: Action): Context

  }

            /*
protected[dima] class DelegationCommand(precedence: Int)
extends GreenTeaCommand[ProactivityOption, Nothing](new Proactivity {

type ReturnType = DelegationCommand.this.Return

val systemPrecedence: Int = precedence
}) {

type Return <: ExecutionStatus
}
       */
