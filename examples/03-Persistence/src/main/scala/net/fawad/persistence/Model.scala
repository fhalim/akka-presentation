package net.fawad.persistence

import akka.actor.ActorRef

case class GoOrderCoffee()
case class CoffeeOrder(amount: Int)
case class GiveCoffee(order: CoffeeOrder)
case class OrderCoffee(order:CoffeeOrder)
case class CoffeeOrdered(customer:ActorRef, order:CoffeeOrder)
case class CoffeeMade(customer:ActorRef, order:CoffeeOrder)
case class PayForCoffee(order:CoffeeOrder, amount: Int)
case class AddMoney(amount:Int)
case class MoneyAdded(amount:Int)