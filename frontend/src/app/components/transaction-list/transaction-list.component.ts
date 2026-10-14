import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { TransactionResponse } from '../../core/models/transaction.model';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe],
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css']
})
export class TransactionListComponent {
  @Input() transactions: TransactionResponse[] = [];
  @Input() loading: boolean = false;
  @Input() error: string | null = null;

  @Output() edit = new EventEmitter<TransactionResponse>();
  @Output() delete = new EventEmitter<TransactionResponse>();
  @Output() retry = new EventEmitter<void>();
  @Output() addClick = new EventEmitter<void>();

  onEdit(transaction: TransactionResponse): void {
    this.edit.emit(transaction);
  }

  onDelete(transaction: TransactionResponse): void {
    this.delete.emit(transaction);
  }

  onRetry(): void {
    this.retry.emit();
  }

  onAdd(): void {
    this.addClick.emit();
  }
}
