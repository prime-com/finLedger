import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionFilter, TransactionType } from '../../core/models/transaction.model';

@Component({
  selector: 'app-transaction-filter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transaction-filter.component.html',
  styleUrls: ['./transaction-filter.component.css']
})
export class TransactionFilterComponent {
  @Output() filterChange = new EventEmitter<TransactionFilter>();
  @Output() filterReset = new EventEmitter<void>();

  filter: TransactionFilter = {
    type: '',
    category: '',
    startDate: '',
    endDate: ''
  };

  categories: string[] = [
    'Salary',
    'Freelance',
    'Investment',
    'Food',
    'Housing',
    'Utilities',
    'Transportation',
    'Entertainment',
    'Health',
    'Shopping',
    'Education',
    'Miscellaneous'
  ];

  applyFilter(): void {
    this.filterChange.emit({ ...this.filter });
  }

  resetFilter(): void {
    this.filter = {
      type: '',
      category: '',
      startDate: '',
      endDate: ''
    };
    this.filterReset.emit();
  }

  hasActiveFilters(): boolean {
    return !!(this.filter.type || this.filter.category || this.filter.startDate || this.filter.endDate);
  }
}
