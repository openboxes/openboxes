package unit.org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionAction
import org.pih.warehouse.inventory.TransactionSource
import spock.lang.Specification

class TransactionSourceSpec extends Specification implements DomainUnitTest<TransactionSource>, DataTest {

    void setupSpec() {
        mockDomains(Transaction, TransactionSource)
    }

    void 'getAssociatedTransactions() should return all transactions connected with a transaction source'() {
        given:
        TransactionSource transactionSource = new TransactionSource(id: 1).save(validate: false)
        Transaction transaction1 = new Transaction(id: 1, transactionSource: transactionSource).save(validate: false)
        Transaction transaction2 = new Transaction(id: 2, transactionSource: transactionSource).save(validate: false)
        Transaction transaction3 = new Transaction(id: 3, transactionSource: transactionSource).save(validate: false)

        expect:
        assert transactionSource.associatedTransactions.size() == 3
    }

    void 'validation should not pass for a transaction source without specified transaction action'() {
        given:
        TransactionSource transactionSource = new TransactionSource(id: 2)
        transactionSource.validate()

        expect:
        assert transactionSource.errors.hasFieldErrors("transactionAction")

    }

    void 'validation should pass for a transaction source with specified transactionAction and not having any source (cycle count, shipment etc)'() {
        given:
        User user = new User()
        TransactionSource transactionSource = new TransactionSource(
                id: 2,
                transactionAction: TransactionAction.RECORD_STOCK,
                createdBy: user,
                updatedBy: user
        )

        expect:
        assert transactionSource.validate()
    }

    void 'record stock should be considered as a count action'() {
        given:
        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.RECORD_STOCK,
        )

        expect:
        assert TransactionAction.isCountAction(transactionSource.transactionAction)
    }

    void 'cycle count, inventory import, record stock, inventory adjustment should be considered as a count action'() {
        given:
        domain.transactionAction = transactionAction

        expect:
        TransactionAction.isCountAction(domain.transactionAction)

        where:
        transactionAction << [
                TransactionAction.CYCLE_COUNT,
                TransactionAction.INVENTORY_IMPORT,
                TransactionAction.RECORD_STOCK,
                TransactionAction.INVENTORY_ADJUSTMENT
        ]
    }
}
