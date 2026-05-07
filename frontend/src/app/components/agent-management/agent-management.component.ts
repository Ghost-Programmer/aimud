import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import EasyMDE from 'easymde';
import { ConfigService } from '../../services/config.service';
import { Agent } from '../../models/agent.model';
import { MarkdownPipe } from '../../pipes/markdown.pipe';

@Component({
  selector: 'app-agent-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MarkdownPipe],
  templateUrl: './agent-management.component.html',
  styleUrl: './agent-management.component.css'
})
export class AgentManagementComponent implements OnInit {
  agents: Agent[] = [];
  totalItems: number = 0;
  page: number = 0;
  size: number = 5;

  selectedAgent: Agent | null = null;
  agentForm: FormGroup;
  isAgentFormVisible = false;
  
  @ViewChild('mdeTextarea') mdeTextarea!: ElementRef<HTMLTextAreaElement>;
  private easyMDE: EasyMDE | null = null;

  constructor(
    private fb: FormBuilder,
    private configService: ConfigService
  ) {
    this.agentForm = this.fb.group({
      id: [null],
      title: ['', Validators.required],
      content: ['', Validators.required]
    });
  }

  get totalAgentPages(): number {
    return Math.max(1, Math.ceil(this.totalItems / this.size));
  }

  get paginatedAgents(): Agent[] {
    return this.agents;
  }

  get agentRangeStart(): number {
    if (this.totalItems === 0) {
      return 0;
    }
    return (this.page * this.size) + 1;
  }

  get agentRangeEnd(): number {
    if (this.totalItems === 0) {
      return 0;
    }
    return Math.min((this.page + 1) * this.size, this.totalItems);
  }

  ngOnInit() {
    this.loadAgents();
  }

  loadAgents() {
    this.configService.getAllAgents(this.page, this.size).subscribe(res => {
      this.agents = res.agents;
      this.totalItems = res.total;
    });
  }

  previousAgentPage() {
    if (this.page > 0) {
      this.page--;
      this.loadAgents();
    }
  }

  nextAgentPage() {
    if ((this.page + 1) * this.size < this.totalItems) {
      this.page++;
      this.loadAgents();
    }
  }

  openAgentForm(agent: Agent | null = null) {
    this.selectedAgent = agent;
    if (agent) {
      this.agentForm.patchValue(agent);
    } else {
      this.agentForm.reset({
        id: null,
        title: '',
        content: ''
      });
    }
    this.isAgentFormVisible = true;

    // Initialize or update EasyMDE
    setTimeout(() => {
      if (this.mdeTextarea && !this.easyMDE) {
        this.easyMDE = new EasyMDE({ 
          element: this.mdeTextarea.nativeElement,
          spellChecker: false,
          maxHeight: '400px'
        });
        
        if (agent) {
          this.easyMDE.value(agent.content || '');
        }

        this.easyMDE.codemirror.on('change', () => {
          this.agentForm.patchValue({ content: this.easyMDE?.value() || '' });
        });
      } else if (this.easyMDE) {
        this.easyMDE.value(agent ? agent.content || '' : '');
      }
    });
  }

  closeAgentForm() {
    if (this.easyMDE) {
      this.easyMDE.toTextArea();
      this.easyMDE = null;
    }
    this.isAgentFormVisible = false;
    this.selectedAgent = null;
  }

  saveAgent() {
    if (this.agentForm.valid) {
      const agent = this.agentForm.value as Agent;
      if (this.selectedAgent?.id) {
        this.configService.updateAgent(this.selectedAgent.id, agent).subscribe(() => {
          this.loadAgents();
          this.closeAgentForm();
        });
      } else {
        this.configService.createAgent(agent).subscribe(() => {
          this.loadAgents();
          this.closeAgentForm();
        });
      }
    }
  }

  deleteAgent(id: number | undefined, event?: Event) {
    event?.stopPropagation();

    if (!id) {
      return;
    }

    if (confirm('Are you sure you want to delete this agent?')) {
      this.configService.deleteAgent(id).subscribe(() => {
        this.loadAgents();
      });
    }
  }


}
