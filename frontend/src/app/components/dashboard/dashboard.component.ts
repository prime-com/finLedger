import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionService } from '../../core/services/transaction.service';
import { ToastService } from '../../core/services/toast.service';
import {
  FinancialSummary,
  TransactionFilter,
  TransactionRequest,
  TransactionResponse
} from '../../core/models/transaction.model';
import { SummaryCardsComponent } from '../summary-cards/summary-cards.component';
import { TransactionFilterComponent } from '../transaction-filter/transaction-filter.component';
import { TransactionListComponent } from '../transaction-list/transaction-list.component';
import { TransactionFormComponent } from '../transaction-form/transaction-form.component';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    SummaryCardsComponent,
    TransactionFilterComponent,
    TransactionListComponent,
    TransactionFormComponent,
    ConfirmDialogComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  transactions: TransactionResponse[] = [];
  summary: FinancialSummary | null = null;

  loadingTransactions: boolean = false;
  loadingSummary: boolean = false;
  errorMessage: string | null = null;

  activeFilter: TransactionFilter = {};

  // Form Modal State
  isFormOpen: boolean = false;
  transactionToEdit: TransactionResponse | null = null;
  isSaving: boolean = false;

  // Delete Confirmation State
  isDeleteOpen: boolean = false;
  transactionToDelete: TransactionResponse | null = null;
  isDeleting: boolean = false;

  constructor(
    private transactionService: TransactionService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.refreshAll();
  }

  refreshAll(): void {
    this.loadTransactions();
    this.loadSummary();
  }

  loadTransactions(): void {
    this.loadingTransactions = true;
    this.errorMessage = null;

    this.transactionService.getTransactions(this.activeFilter).subscribe({
      next: (data) => {
        this.transactions = data;
        this.loadingTransactions = false;
      },
      error: (err) => {
        this.loadingTransactions = false;
        const msg = err.message || 'Failed to connect to FinLedger backend.';
        this.errorMessage = msg;
        this.toastService.error(msg, 'Network Error');
      }
    });
  }

  loadSummary(): void {
    this.loadingSummary = true;
    this.transactionService.getFinancialSummary(this.activeFilter.startDate, this.activeFilter.endDate).subscribe({
      next: (data) => {
        this.summary = data;
        this.loadingSummary = false;
      },
      error: (err) => {
        this.loadingSummary = false;
        console.error('Failed to load summary:', err);
      }
    });
  }

  onFilterChange(filter: TransactionFilter): void {
    this.activeFilter = filter;
    this.refreshAll();
  }

  onFilterReset(): void {
    this.activeFilter = {};
    this.refreshAll();
  }

  openCreateModal(): void {
    this.transactionToEdit = null;
    this.isFormOpen = true;
  }

  openEditModal(transaction: TransactionResponse): void {
    this.transactionToEdit = transaction;
    this.isFormOpen = true;
  }

  closeModal(): void {
    this.isFormOpen = false;
    this.transactionToEdit = null;
  }

  saveTransaction(request: TransactionRequest): void {
    this.isSaving = true;

    if (this.transactionToEdit) {
      // Edit mode
      this.transactionService.updateTransaction(this.transactionToEdit.id, request).subscribe({
        next: (updated) => {
          this.isSaving = false;
          this.closeModal();
          this.toastService.success(`Updated "${updated.title}" successfully.`);
          this.refreshAll();
        },
        error: (err) => {
          this.isSaving = false;
          this.toastService.error(err.message || 'Failed to update transaction.');
        }
      });
    } else {
      // Create mode
      this.transactionService.createTransaction(request).subscribe({
        next: (created) => {
          this.isSaving = false;
          this.closeModal();
          this.toastService.success(`Recorded "${created.title}" successfully.`);
          this.refreshAll();
        },
        error: (err) => {
          this.isSaving = false;
          this.toastService.error(err.message || 'Failed to create transaction.');
        }
      });
    }
  }

  openDeleteDialog(transaction: TransactionResponse): void {
    this.transactionToDelete = transaction;
    this.isDeleteOpen = true;
  }

  closeDeleteDialog(): void {
    this.isDeleteOpen = false;
    this.transactionToDelete = null;
  }

  confirmDelete(): void {
    if (!this.transactionToDelete) return;

    this.isDeleting = true;
    const tx = this.transactionToDelete;

    this.transactionService.deleteTransaction(tx.id).subscribe({
      next: () => {
        this.isDeleting = false;
        this.closeDeleteDialog();
        this.toastService.success(`Transaction "${tx.title}" deleted.`);
        this.refreshAll();
      },
      error: (err) => {
        this.isDeleting = false;
        this.toastService.error(err.message || 'Failed to delete transaction.');
      }
    });
  }
}
